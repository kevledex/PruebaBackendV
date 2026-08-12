package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.enums.CaracterEvaluacion;
import com.itsqmet.aplicativoweb.enums.TipoEvaluacion;
import com.itsqmet.aplicativoweb.exception.OperacionNoPermitidaException;
import com.itsqmet.aplicativoweb.model.Actividad;
import com.itsqmet.aplicativoweb.repository.ActividadRepository;
import com.itsqmet.aplicativoweb.repository.PeriodoAcademicoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * AJUSTADO: se agregan las validaciones de negocio que antes no existían:
 *  - Art. 16 del Acuerdo: una evaluación ANTICIPADA debe registrarse con al
 *    menos 15 días de anticipación (30 si es una evaluación final).
 *  - Art. 17 del Acuerdo: una evaluación ATRASADA debe aplicarse dentro de
 *    los 5 días posteriores al retorno del estudiante (15 si es una
 *    evaluación final).
 *  - No se permite registrar evaluaciones sobre un PeriodoAcademico
 *    cerrado.
 *  - Un proyecto interdisciplinar debe ser de tipo SUMATIVA (Cap. 2.3-2.4
 *    del Instructivo).
 */
@Service

public class ActividadService {
    private final ActividadRepository actividadRepository;
    private final PeriodoAcademicoRepository periodoAcademicoRepository;

    public ActividadService(ActividadRepository actividadRepository,
                             PeriodoAcademicoRepository periodoAcademicoRepository) {
        this.actividadRepository = actividadRepository;
        this.periodoAcademicoRepository = periodoAcademicoRepository;
    }

    public List<Actividad> obtenerTodas(Long materiaCursoId, Long periodoAcademicoId){
        if (materiaCursoId != null && periodoAcademicoId != null) {
            return actividadRepository.findByMateriaCursoIdAndPeriodoAcademicoId(materiaCursoId, periodoAcademicoId);
        }
        return actividadRepository.findAll();
    }

    public Optional<Actividad> obtenerPorId(Long id){
        return actividadRepository.findById(id);
    }

    public Actividad guardarActividad(Actividad actividad){
        validarPeriodoAbierto(actividad);
        validarProyectoInterdisciplinar(actividad);
        return actividadRepository.save(actividad);
    }

    public Actividad registrarAnticipada(Actividad actividad, boolean esEvaluacionFinal) {
        long diasMinimos = esEvaluacionFinal ? 30 : 15;
        if (ChronoUnit.DAYS.between(LocalDate.now(), actividad.getFecha()) < diasMinimos) {
            throw new OperacionNoPermitidaException(
                    "Una evaluación anticipada debe solicitarse al menos " + diasMinimos
                            + " días antes de la fecha del cronograma (Art. 16 del Acuerdo Ministerial)");
        }
        actividad.setCaracter(CaracterEvaluacion.ANTICIPADA);
        return guardarActividad(actividad);
    }

    public Actividad registrarAtrasada(Actividad actividad, boolean esEvaluacionFinal) {
        long diasMaximos = esEvaluacionFinal ? 15 : 5;
        if (ChronoUnit.DAYS.between(actividad.getFecha(), LocalDate.now()) > diasMaximos) {
            throw new OperacionNoPermitidaException(
                    "Una evaluación atrasada debe aplicarse dentro de los " + diasMaximos
                            + " días posteriores al retorno del estudiante (Art. 17 del Acuerdo Ministerial)");
        }
        actividad.setCaracter(CaracterEvaluacion.ATRASADA);
        return guardarActividad(actividad);
    }

    public void eliminarActividad(Long id){
        actividadRepository.deleteById(id);
    }

    /**
     * CORREGIDO: el frontend solo envía "{ periodoAcademico: { id } }" (sin
     * el resto de campos), así que "actividad.getPeriodoAcademico()" traía
     * un objeto parcial deserializado por Jackson con "cerrado" en su valor
     * por defecto (false) SIN IMPORTAR el estado real en la base de datos.
     * Como resultado, esta validación nunca bloqueaba nada: se podían crear
     * o editar actividades/notas sobre un periodo ya cerrado. Se corrige
     * consultando el estado real en la base de datos.
     *
     * CORREGIDO: una primera versión de este fix releía el PeriodoAcademico
     * COMPLETO (findById) y lo dejaba enganchado en la actividad
     * ("actividad.setPeriodoAcademico(periodoReal)"). Al guardar/serializar
     * la actividad, Jackson recorría esa entidad ya completamente hidratada
     * (curso -> anioLectivo/tutor -> rol -> permisos), y Hibernate iba
     * resolviendo cada asociación LAZY con una consulta adicional: guardar
     * una sola actividad pasaba de 1 consulta a decenas. Multiplicado por
     * cada actividad y cada nota de una tabla real (Elemental/Media guardan
     * en un bucle secuencial, una petición a la vez), el guardado de una
     * clase completa podía tardar minutos y el botón "Guardar" parecía
     * quedarse colgado para siempre. Se reemplaza por una consulta liviana
     * que solo trae el booleano "cerrado" y no toca la actividad en absoluto.
     */
    private void validarPeriodoAbierto(Actividad actividad) {
        if (actividad.getPeriodoAcademico() == null || actividad.getPeriodoAcademico().getId() == null) {
            return;
        }
        if (periodoAcademicoRepository.existsByIdAndCerradoTrue(actividad.getPeriodoAcademico().getId())) {
            throw new OperacionNoPermitidaException(
                    "El periodo académico ya está cerrado; no se pueden registrar más evaluaciones sobre él");
        }
    }

    private void validarProyectoInterdisciplinar(Actividad actividad) {
        if (actividad.isEsProyectoInterdisciplinar() && actividad.getTipoEvaluacion() != TipoEvaluacion.SUMATIVA) {
            throw new OperacionNoPermitidaException(
                    "Un proyecto interdisciplinar debe registrarse como evaluación de tipo SUMATIVA (Cap. 2.3 del Instructivo)");
        }
    }
}
