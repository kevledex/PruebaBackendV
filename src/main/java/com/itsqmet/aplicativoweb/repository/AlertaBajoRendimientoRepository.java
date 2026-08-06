package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.enums.EstadoAlerta;
import com.itsqmet.aplicativoweb.model.AlertaBajoRendimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * "countByEstadoNot" es usado por ReporteService para el indicador de
 * alertas activas del dashboard.
 */
public interface AlertaBajoRendimientoRepository extends JpaRepository<AlertaBajoRendimiento, Long> {
    List<AlertaBajoRendimiento> findByAlumnoId(Long alumnoId);
    List<AlertaBajoRendimiento> findByPeriodoAcademicoId(Long periodoAcademicoId);
    List<AlertaBajoRendimiento> findByEstado(EstadoAlerta estado);
    Optional<AlertaBajoRendimiento> findByAlumnoIdAndMateriaCursoIdAndPeriodoAcademicoIdAndEstadoNot(
            Long alumnoId, Long materiaCursoId, Long periodoAcademicoId, EstadoAlerta estadoExcluido);
    long countByEstadoNot(EstadoAlerta estado);
}
