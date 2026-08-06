package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.enums.EstadoSolicitud;
import com.itsqmet.aplicativoweb.model.SolicitudRevision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * "countByEstadoIn" es usado por ReporteService para el indicador de
 * solicitudes de revisión/apelación pendientes del dashboard.
 */
public interface SolicitudRevisionRepository extends JpaRepository<SolicitudRevision, Long> {
    List<SolicitudRevision> findBySolicitanteId(Long representanteId);
    long countByEstadoIn(List<EstadoSolicitud> estados);
}
