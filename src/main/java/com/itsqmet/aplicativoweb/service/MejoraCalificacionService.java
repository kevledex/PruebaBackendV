package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.dto.MejoraDtos;
import com.itsqmet.aplicativoweb.enums.CaracterEvaluacion;
import com.itsqmet.aplicativoweb.enums.EstadoRefuerzo;
import com.itsqmet.aplicativoweb.exception.OperacionNoPermitidaException;
import com.itsqmet.aplicativoweb.exception.RecursoNoEncontradoException;
import com.itsqmet.aplicativoweb.model.MateriaCurso;
import com.itsqmet.aplicativoweb.model.Nota;
import com.itsqmet.aplicativoweb.model.RefuerzoPedagogico;
import com.itsqmet.aplicativoweb.repository.MateriaCursoRepository;
import com.itsqmet.aplicativoweb.repository.NotaRepository;
import com.itsqmet.aplicativoweb.repository.RefuerzoPedagogicoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * NUEVO. Implementa las reglas de mejora de calificaciones que antes no
 * existían en absoluto (NotaController solo hacía CRUD libre, sin ningún
 * tope):
 *  - Mejora directa (Art. 10 y 11 del Acuerdo): calificación inicial entre
 *    7.00 y 9.00 (exclusive); máximo 1 evaluación por asignatura y periodo
 *    académico; máximo 3 en el año lectivo; el promedio de mejora se topa
 *    en 9.00; si el promedio resultante es menor al inicial, se conserva
 *    el inicial.
 *  - Mejora con refuerzo pedagógico (Art. 10 y 12 del Acuerdo): calificación
 *    inicial entre 0.01 y 6.99; máximo 2 por asignatura EN TODO EL AÑO
 *    LECTIVO (a diferencia de la mejora directa, aquí el tope no es por
 *    periodo); máximo 6 en el año lectivo; requiere un RefuerzoPedagogico
 *    FINALIZADO; el promedio de mejora es el promedio simple entre
 *    calificación inicial + calificación del refuerzo + evaluación
 *    adicional (Tabla 21 del Instructivo).
 */
@Service
public class MejoraCalificacionService {

    private static final BigDecimal LIMITE_INFERIOR_MEJORA_DIRECTA = new BigDecimal("7.00");
    private static final BigDecimal TOPE_MEJORA_DIRECTA = new BigDecimal("9.00");
    private static final BigDecimal LIMITE_SUPERIOR_MEJORA_REFUERZO = new BigDecimal("6.99");

    private static final int MAX_DIRECTA_POR_MATERIA_PERIODO = 1;
    private static final int MAX_DIRECTA_POR_ANIO = 3;
    private static final int MAX_REFUERZO_POR_MATERIA = 2;
    private static final int MAX_REFUERZO_POR_ANIO = 6;

    private final NotaRepository notaRepository;
    private final MateriaCursoRepository materiaCursoRepository;
    private final RefuerzoPedagogicoRepository refuerzoPedagogicoRepository;

    public MejoraCalificacionService(NotaRepository notaRepository,
                                      MateriaCursoRepository materiaCursoRepository,
                                      RefuerzoPedagogicoRepository refuerzoPedagogicoRepository) {
        this.notaRepository = notaRepository;
        this.materiaCursoRepository = materiaCursoRepository;
        this.refuerzoPedagogicoRepository = refuerzoPedagogicoRepository;
    }

    public Nota solicitarMejoraDirecta(Long notaOriginalId, Nota evaluacionMejora) {
        Nota notaOriginal = notaRepository.findById(notaOriginalId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Nota", notaOriginalId));

        if (notaOriginal.getCalificacion().compareTo(LIMITE_INFERIOR_MEJORA_DIRECTA) <= 0
                || notaOriginal.getCalificacion().compareTo(TOPE_MEJORA_DIRECTA) >= 0) {
            throw new OperacionNoPermitidaException(
                    "La mejora directa solo aplica a calificaciones entre 7.00 y 9.00 (Art. 10 y 11 del Acuerdo Ministerial)");
        }

        Long alumnoId = notaOriginal.getAlumno().getId();
        Long materiaCursoId = notaOriginal.getActividad().getMateriaCurso().getId();
        Long periodoId = notaOriginal.getActividad().getPeriodoAcademico().getId();
        Long anioLectivoId = notaOriginal.getActividad().getMateriaCurso().getCurso().getAnioLectivo().getId();

        long usadasEnPeriodo = notaRepository
                .countByAlumnoIdAndActividad_MateriaCursoIdAndActividad_PeriodoAcademicoIdAndActividad_Caracter(
                        alumnoId, materiaCursoId, periodoId, CaracterEvaluacion.MEJORA_DIRECTA);
        if (usadasEnPeriodo >= MAX_DIRECTA_POR_MATERIA_PERIODO) {
            throw new OperacionNoPermitidaException(
                    "Ya se usó la mejora directa disponible para esta asignatura en este periodo académico (Art. 11 del Acuerdo Ministerial)");
        }

        long usadasEnAnio = notaRepository
                .countByAlumnoIdAndActividad_MateriaCurso_Curso_AnioLectivoIdAndActividad_Caracter(
                        alumnoId, anioLectivoId, CaracterEvaluacion.MEJORA_DIRECTA);
        if (usadasEnAnio >= MAX_DIRECTA_POR_ANIO) {
            throw new OperacionNoPermitidaException(
                    "Se alcanzó el máximo de 3 evaluaciones de mejora directa en el año lectivo (Art. 11 del Acuerdo Ministerial)");
        }

        evaluacionMejora.getActividad().setCaracter(CaracterEvaluacion.MEJORA_DIRECTA);
        evaluacionMejora.setAlumno(notaOriginal.getAlumno());
        evaluacionMejora.setEsNotaDeMejora(true);
        evaluacionMejora.setNotaOriginal(notaOriginal);

        BigDecimal calificacionEvaluacionAdicional = NotaService.truncar2Decimales(evaluacionMejora.getCalificacion());
        BigDecimal promedioMejora = NotaService.truncar2Decimales(
                notaOriginal.getCalificacion().add(calificacionEvaluacionAdicional)
                        .divide(new BigDecimal("2"), 10, RoundingMode.DOWN));
        if (promedioMejora.compareTo(TOPE_MEJORA_DIRECTA) > 0) {
            promedioMejora = TOPE_MEJORA_DIRECTA;
        }
        if (promedioMejora.compareTo(notaOriginal.getCalificacion()) < 0) {
            // Art. 11: si el promedio de mejora es inferior al inicial, se mantiene el inicial sin modificación.
            return notaOriginal;
        }

        evaluacionMejora.setCalificacion(promedioMejora);
        return notaRepository.save(evaluacionMejora);
    }

    public Nota solicitarMejoraConRefuerzo(Long notaOriginalId,
                                            Long refuerzoPedagogicoId,
                                            BigDecimal calificacionRefuerzoPedagogico,
                                            Nota evaluacionAdicional) {
        Nota notaOriginal = notaRepository.findById(notaOriginalId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Nota", notaOriginalId));

        if (notaOriginal.getCalificacion().compareTo(LIMITE_SUPERIOR_MEJORA_REFUERZO) > 0) {
            throw new OperacionNoPermitidaException(
                    "La mejora con refuerzo pedagógico solo aplica a calificaciones entre 0.01 y 6.99 (Art. 10 y 12 del Acuerdo Ministerial)");
        }

        RefuerzoPedagogico refuerzo = refuerzoPedagogicoRepository.findById(refuerzoPedagogicoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("RefuerzoPedagogico", refuerzoPedagogicoId));
        if (refuerzo.getEstado() != EstadoRefuerzo.FINALIZADO) {
            throw new OperacionNoPermitidaException(
                    "El refuerzo pedagógico debe estar FINALIZADO antes de aplicar la evaluación de mejora (Art. 12 del Acuerdo Ministerial)");
        }

        Long alumnoId = notaOriginal.getAlumno().getId();
        Long materiaCursoId = notaOriginal.getActividad().getMateriaCurso().getId();
        Long anioLectivoId = notaOriginal.getActividad().getMateriaCurso().getCurso().getAnioLectivo().getId();

        long usadasEnMateria = notaRepository.countByAlumnoIdAndActividad_MateriaCursoIdAndActividad_Caracter(
                alumnoId, materiaCursoId, CaracterEvaluacion.MEJORA_CON_REFUERZO);
        if (usadasEnMateria >= MAX_REFUERZO_POR_MATERIA) {
            throw new OperacionNoPermitidaException(
                    "Ya se usaron las 2 mejoras con refuerzo disponibles para esta asignatura en el año lectivo (Art. 12 del Acuerdo Ministerial)");
        }

        long usadasEnAnio = notaRepository
                .countByAlumnoIdAndActividad_MateriaCurso_Curso_AnioLectivoIdAndActividad_Caracter(
                        alumnoId, anioLectivoId, CaracterEvaluacion.MEJORA_CON_REFUERZO);
        if (usadasEnAnio >= MAX_REFUERZO_POR_ANIO) {
            throw new OperacionNoPermitidaException(
                    "Se alcanzó el máximo de 6 mejoras con refuerzo pedagógico en el año lectivo (Art. 12 del Acuerdo Ministerial)");
        }

        evaluacionAdicional.getActividad().setCaracter(CaracterEvaluacion.MEJORA_CON_REFUERZO);
        evaluacionAdicional.setAlumno(notaOriginal.getAlumno());
        evaluacionAdicional.setEsNotaDeMejora(true);
        evaluacionAdicional.setNotaOriginal(notaOriginal);

        // Tabla 21 del Instructivo: promedio simple entre calificación inicial,
        // calificación del refuerzo pedagógico y evaluación adicional.
        BigDecimal calificacionEvaluacionAdicional = NotaService.truncar2Decimales(evaluacionAdicional.getCalificacion());
        BigDecimal calificacionRefuerzo = NotaService.truncar2Decimales(calificacionRefuerzoPedagogico);
        BigDecimal promedioMejora = NotaService.truncar2Decimales(
                notaOriginal.getCalificacion()
                        .add(calificacionRefuerzo)
                        .add(calificacionEvaluacionAdicional)
                        .divide(new BigDecimal("3"), 10, RoundingMode.DOWN));

        if (promedioMejora.compareTo(notaOriginal.getCalificacion()) < 0) {
            // Art. 12: si el promedio de mejora es inferior al inicial, se mantiene el inicial sin modificación.
            return notaOriginal;
        }

        evaluacionAdicional.setCalificacion(promedioMejora);
        return notaRepository.save(evaluacionAdicional);
    }

    public MejoraDtos.CupoMejora obtenerCupos(Long alumnoId, Long materiaCursoId, Long periodoAcademicoId) {
        MateriaCurso materiaCurso = materiaCursoRepository.findById(materiaCursoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("MateriaCurso", materiaCursoId));
        Long anioLectivoId = materiaCurso.getCurso().getAnioLectivo().getId();

        int directasMateriaPeriodo = (int) notaRepository
                .countByAlumnoIdAndActividad_MateriaCursoIdAndActividad_PeriodoAcademicoIdAndActividad_Caracter(
                        alumnoId, materiaCursoId, periodoAcademicoId, CaracterEvaluacion.MEJORA_DIRECTA);
        int directasAnio = (int) notaRepository
                .countByAlumnoIdAndActividad_MateriaCurso_Curso_AnioLectivoIdAndActividad_Caracter(
                        alumnoId, anioLectivoId, CaracterEvaluacion.MEJORA_DIRECTA);
        int refuerzoMateria = (int) notaRepository.countByAlumnoIdAndActividad_MateriaCursoIdAndActividad_Caracter(
                alumnoId, materiaCursoId, CaracterEvaluacion.MEJORA_CON_REFUERZO);
        int refuerzoAnio = (int) notaRepository
                .countByAlumnoIdAndActividad_MateriaCurso_Curso_AnioLectivoIdAndActividad_Caracter(
                        alumnoId, anioLectivoId, CaracterEvaluacion.MEJORA_CON_REFUERZO);

        return new MejoraDtos.CupoMejora(
                alumnoId, materiaCursoId,
                directasMateriaPeriodo, directasAnio,
                refuerzoMateria, refuerzoAnio,
                Math.max(0, MAX_DIRECTA_POR_ANIO - directasAnio),
                Math.max(0, MAX_REFUERZO_POR_ANIO - refuerzoAnio));
    }
}
