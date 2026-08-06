package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.enums.NivelEducativo;
import com.itsqmet.aplicativoweb.enums.TipoPromocion;
import com.itsqmet.aplicativoweb.exception.OperacionNoPermitidaException;
import com.itsqmet.aplicativoweb.exception.RecursoNoEncontradoException;
import com.itsqmet.aplicativoweb.model.*;
import com.itsqmet.aplicativoweb.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * NUEVO. Implementa el cálculo de promoción/repitencia que antes no existía
 * (Alumno solo tenía un campo genérico "estado", sin resultado de
 * promoción).
 *
 * AJUSTADO: esta institución solo ofrece hasta 7mo EGB (Inicial,
 * Preparatoria, Elemental y Subnivel Media) -- no existen Superior ni
 * Bachillerato, así que todo estudiante que no se promocione
 * automáticamente pertenece siempre al Subnivel Media, y por lo tanto la
 * excepción de "promoción excepcional con condiciones" (en vez de
 * repitencia directa) se aplica siempre que corresponda repetir, sin
 * necesidad de distinguir el nivel:
 *  - Art. 19: automática en Inicial/Preparatoria/Elemental.
 *  - Art. 20: directa en el Subnivel Media si todas las materias que
 *    cuentan para promoción tienen promedio final >= 7.00.
 *  - Art. 21/22: si alguna materia queda entre 4.01-6.99, corresponde
 *    supletoria; tras aplicarla (ver SupletoriaService) se debe llamar a
 *    "recalcularTrasSupletoria".
 *  - Art. 24/25: si alguna materia queda en 4.00 o menos (o sigue así tras
 *    la supletoria), se activa la "promoción excepcional con condiciones"
 *    (refuerzo pedagógico + acuerdo consensuado con el representante +
 *    informe de Junta de Docentes) en vez de repitencia directa.
 *  - Art. 23: repitencia excepcional en Preparatoria/Elemental, aplicable
 *    una única vez por estudiante.
 */
@Service
public class PromocionService {

    private static final BigDecimal PROMEDIO_MINIMO_DIRECTA = new BigDecimal("7.00");
    private static final BigDecimal PROMEDIO_REPITENCIA_DIRECTA = new BigDecimal("4.00");

    private final RegistroPromocionRepository registroPromocionRepository;
    private final MateriaCursoRepository materiaCursoRepository;
    private final PromedioService promedioService;
    private final AlumnoRepository alumnoRepository;
    private final RefuerzoPedagogicoRepository refuerzoPedagogicoRepository;
    private final EvaluacionPsicopedagogicaRepository evaluacionPsicopedagogicaRepository;

    public PromocionService(RegistroPromocionRepository registroPromocionRepository,
                             MateriaCursoRepository materiaCursoRepository,
                             PromedioService promedioService,
                             AlumnoRepository alumnoRepository,
                             RefuerzoPedagogicoRepository refuerzoPedagogicoRepository,
                             EvaluacionPsicopedagogicaRepository evaluacionPsicopedagogicaRepository) {
        this.registroPromocionRepository = registroPromocionRepository;
        this.materiaCursoRepository = materiaCursoRepository;
        this.promedioService = promedioService;
        this.alumnoRepository = alumnoRepository;
        this.refuerzoPedagogicoRepository = refuerzoPedagogicoRepository;
        this.evaluacionPsicopedagogicaRepository = evaluacionPsicopedagogicaRepository;
    }

    public RegistroPromocion calcularPromocion(Long alumnoId, Long anioLectivoId) {
        Alumno alumno = alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno", alumnoId));
        Curso curso = alumno.getCurso();
        NivelEducativo nivel = curso.getNivel();

        RegistroPromocion registro = registroPromocionRepository.findByAlumnoIdAndAnioLectivoId(alumnoId, anioLectivoId)
                .orElseGet(RegistroPromocion::new);
        registro.setAlumno(alumno);
        registro.setCursoOrigen(curso);
        registro.setAnioLectivo(curso.getAnioLectivo());

        if (nivel == NivelEducativo.INICIAL || nivel == NivelEducativo.PREPARATORIA || nivel == NivelEducativo.ELEMENTAL) {
            registro.setTipoPromocion(TipoPromocion.AUTOMATICA);
            registro.setFechaResolucion(LocalDate.now());
            return registroPromocionRepository.save(registro);
        }

        List<MateriaCurso> materiasQueCuentan = materiaCursoRepository.findByCursoId(curso.getId()).stream()
                .filter(materiaCurso -> materiaCurso.getMateria().isCuentaParaPromocion())
                .toList();

        boolean todasSobreOIgualA7 = true;
        boolean algunaMenorOIgualA4 = false;
        boolean algunaEntre4y7 = false;

        for (MateriaCurso materiaCurso : materiasQueCuentan) {
            BigDecimal promedioAnual = promedioService.calcularPromedioAnual(alumnoId, materiaCurso.getId());
            if (promedioAnual.compareTo(PROMEDIO_MINIMO_DIRECTA) < 0) {
                todasSobreOIgualA7 = false;
                if (promedioAnual.compareTo(PROMEDIO_REPITENCIA_DIRECTA) <= 0) {
                    algunaMenorOIgualA4 = true;
                } else {
                    algunaEntre4y7 = true;
                }
            }
        }

        if (todasSobreOIgualA7) {
            registro.setTipoPromocion(TipoPromocion.DIRECTA);
        } else if (algunaMenorOIgualA4) {
            // Al no existir Superior ni Bachillerato en esta institución, todo
            // estudiante que llega hasta aquí pertenece al Subnivel Media
            // (Art. 24: promoción excepcional con condiciones en vez de repetir).
            registro.setTipoPromocion(TipoPromocion.EXCEPCIONAL_CON_CONDICIONES);
        } else if (algunaEntre4y7) {
            // Requiere supletoria; el resultado definitivo se fija con recalcularTrasSupletoria.
            registro.setTipoPromocion(TipoPromocion.PENDIENTE);
        }

        registro.setFechaResolucion(LocalDate.now());
        return registroPromocionRepository.save(registro);
    }

    public RegistroPromocion recalcularTrasSupletoria(Long alumnoId, Long anioLectivoId) {
        RegistroPromocion registro = obtenerPorAlumnoYAnio(alumnoId, anioLectivoId);
        Curso curso = registro.getCursoOrigen();

        List<MateriaCurso> materiasQueCuentan = materiaCursoRepository.findByCursoId(curso.getId()).stream()
                .filter(materiaCurso -> materiaCurso.getMateria().isCuentaParaPromocion())
                .toList();

        boolean todasSobreOIgualA7 = materiasQueCuentan.stream()
                .allMatch(materiaCurso -> promedioService.calcularPromedioAnual(alumnoId, materiaCurso.getId())
                        .compareTo(PROMEDIO_MINIMO_DIRECTA) >= 0);

        if (todasSobreOIgualA7) {
            registro.setTipoPromocion(TipoPromocion.TRAS_SUPLETORIA);
        } else {
            // Sin Superior ni Bachillerato en esta institución, quien no alcanza
            // el promedio tras la supletoria siempre pertenece al Subnivel Media.
            registro.setTipoPromocion(TipoPromocion.EXCEPCIONAL_CON_CONDICIONES);
        }
        registro.setFechaResolucion(LocalDate.now());
        return registroPromocionRepository.save(registro);
    }

    public RegistroPromocion registrarExcepcionalMedia(Long registroId, Long refuerzoPedagogicoId,
                                                         boolean acuerdoConsensuadoFirmado, String informeJuntaDocentes) {
        RegistroPromocion registro = obtenerPorId(registroId);
        if (registro.getCursoOrigen().getNivel() != NivelEducativo.MEDIA) {
            throw new OperacionNoPermitidaException(
                    "La promoción excepcional con condiciones solo aplica al Subnivel Media (Art. 24 y 25 del Acuerdo Ministerial); "
                            + "esta institución no tiene Superior ni Bachillerato");
        }

        RefuerzoPedagogico refuerzo = refuerzoPedagogicoRepository.findById(refuerzoPedagogicoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("RefuerzoPedagogico", refuerzoPedagogicoId));

        // Instructivo, pág. 42: sin plan de refuerzo, acuerdo firmado e informe de
        // Junta de Docentes completos, el estudiante debe repetir el año.
        if (!acuerdoConsensuadoFirmado || informeJuntaDocentes == null || informeJuntaDocentes.isBlank()) {
            registro.setTipoPromocion(TipoPromocion.REPITE);
            registro.setFechaResolucion(LocalDate.now());
            return registroPromocionRepository.save(registro);
        }

        registro.setPlanRefuerzo(refuerzo);
        registro.setAcuerdoConsensuadoFirmado(true);
        registro.setFechaAcuerdoConsensuado(LocalDate.now());
        registro.setInformeJuntaDocentes(informeJuntaDocentes);
        registro.setTipoPromocion(TipoPromocion.EXCEPCIONAL_CON_CONDICIONES);
        registro.setFechaResolucion(LocalDate.now());
        return registroPromocionRepository.save(registro);
    }

    public RegistroPromocion registrarRepitenciaExcepcional(Long alumnoId, Long anioLectivoId,
                                                              Long evaluacionPsicopedagogicaId,
                                                              boolean solicitudRepresentanteRecibida,
                                                              String informeJuntaDocentes) {
        if (registroPromocionRepository.existsByAlumnoIdAndYaAplicoRepitenciaExcepcionalAntesTrue(alumnoId)) {
            throw new OperacionNoPermitidaException(
                    "La repitencia excepcional en Preparatoria/Elemental solo puede aplicarse una única vez por estudiante (Art. 23 del Acuerdo Ministerial)");
        }

        EvaluacionPsicopedagogica evaluacion = evaluacionPsicopedagogicaRepository.findById(evaluacionPsicopedagogicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("EvaluacionPsicopedagogica", evaluacionPsicopedagogicaId));

        RegistroPromocion registro = registroPromocionRepository.findByAlumnoIdAndAnioLectivoId(alumnoId, anioLectivoId)
                .orElseGet(() -> {
                    Alumno alumno = alumnoRepository.findById(alumnoId)
                            .orElseThrow(() -> new RecursoNoEncontradoException("Alumno", alumnoId));
                    RegistroPromocion nuevo = new RegistroPromocion();
                    nuevo.setAlumno(alumno);
                    nuevo.setCursoOrigen(alumno.getCurso());
                    nuevo.setAnioLectivo(alumno.getCurso().getAnioLectivo());
                    return nuevo;
                });

        registro.setEvaluacionPsicopedagogicaSoporte(evaluacion);
        registro.setSolicitudRepresentanteRecibida(solicitudRepresentanteRecibida);
        registro.setInformeJuntaDocentes(informeJuntaDocentes);
        registro.setYaAplicoRepitenciaExcepcionalAntes(true);
        registro.setTipoPromocion(TipoPromocion.REPITE);
        registro.setFechaResolucion(LocalDate.now());
        return registroPromocionRepository.save(registro);
    }

    public RegistroPromocion obtenerPorId(Long id) {
        return registroPromocionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("RegistroPromocion", id));
    }

    public RegistroPromocion obtenerPorAlumnoYAnio(Long alumnoId, Long anioLectivoId) {
        return registroPromocionRepository.findByAlumnoIdAndAnioLectivoId(alumnoId, anioLectivoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un cálculo de promoción para ese alumno en ese año lectivo"));
    }
}
