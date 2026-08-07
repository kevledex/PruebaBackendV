package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.enums.CaracterEvaluacion;
import com.itsqmet.aplicativoweb.enums.TipoEvaluacion;
import com.itsqmet.aplicativoweb.model.Nota;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * CORREGIDO/AMPLIADO: "findByActividadId" ya existía pero era inválida
 * porque Nota no tenía campo "actividad" (ver Nota.java). Ahora sí es una
 * derived query válida. Se agregan además las consultas que necesitan
 * PromedioService (cálculo de promedios formativa/sumativa por periodo) y
 * MejoraCalificacionService (conteo de cupos de mejora usados por materia
 * y por año lectivo, Art. 11 y 12 del Acuerdo).
 */
public interface NotaRepository extends JpaRepository<Nota, Long> {
    List<Nota> findByActividadId(Long actividadId);
    Optional<Nota> findByAlumnoIdAndActividadId(Long alumnoId, Long actividadId);

    List<Nota> findByAlumnoIdAndActividad_MateriaCursoIdAndActividad_PeriodoAcademicoId(
            Long alumnoId, Long materiaCursoId, Long periodoAcademicoId);

    List<Nota> findByActividad_MateriaCursoIdAndActividad_PeriodoAcademicoId(
            Long materiaCursoId, Long periodoAcademicoId);

    List<Nota> findByAlumnoIdAndActividad_MateriaCursoIdAndActividad_PeriodoAcademicoIdAndActividad_TipoEvaluacion(
            Long alumnoId, Long materiaCursoId, Long periodoAcademicoId, TipoEvaluacion tipoEvaluacion);

    long countByAlumnoIdAndActividad_MateriaCursoIdAndActividad_PeriodoAcademicoIdAndActividad_Caracter(
            Long alumnoId, Long materiaCursoId, Long periodoAcademicoId, CaracterEvaluacion caracter);

    long countByAlumnoIdAndActividad_MateriaCursoIdAndActividad_Caracter(
            Long alumnoId, Long materiaCursoId, CaracterEvaluacion caracter);

    long countByAlumnoIdAndActividad_MateriaCurso_Curso_AnioLectivoIdAndActividad_Caracter(
            Long alumnoId, Long anioLectivoId, CaracterEvaluacion caracter);

    List<Nota> findByActividad_PeriodoAcademicoId(Long periodoAcademicoId);
}
