package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.dto.ReporteDtos;
import com.itsqmet.aplicativoweb.enums.EstadoAlerta;
import com.itsqmet.aplicativoweb.enums.EstadoSolicitud;
import com.itsqmet.aplicativoweb.repository.AlertaBajoRendimientoRepository;
import com.itsqmet.aplicativoweb.repository.AlumnoRepository;
import com.itsqmet.aplicativoweb.repository.CursoRepository;
import com.itsqmet.aplicativoweb.repository.DocenteRepository;
import com.itsqmet.aplicativoweb.repository.MateriaRepository;
import com.itsqmet.aplicativoweb.repository.SolicitudRevisionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * NUEVO: ReporteController ya invocaba "reporteService.generarResumen(...)"
 * pero la clase no existía. Se implementa un resumen con los contadores
 * principales, incluyendo los nuevos procesos normativos (alertas de bajo
 * rendimiento activas y solicitudes de revisión pendientes).
 */
@Service
public class ReporteService {

    private final AlumnoRepository alumnoRepository;
    private final DocenteRepository docenteRepository;
    private final MateriaRepository materiaRepository;
    private final CursoRepository cursoRepository;
    private final AlertaBajoRendimientoRepository alertaBajoRendimientoRepository;
    private final SolicitudRevisionRepository solicitudRevisionRepository;

    public ReporteService(AlumnoRepository alumnoRepository,
                           DocenteRepository docenteRepository,
                           MateriaRepository materiaRepository,
                           CursoRepository cursoRepository,
                           AlertaBajoRendimientoRepository alertaBajoRendimientoRepository,
                           SolicitudRevisionRepository solicitudRevisionRepository) {
        this.alumnoRepository = alumnoRepository;
        this.docenteRepository = docenteRepository;
        this.materiaRepository = materiaRepository;
        this.cursoRepository = cursoRepository;
        this.alertaBajoRendimientoRepository = alertaBajoRendimientoRepository;
        this.solicitudRevisionRepository = solicitudRevisionRepository;
    }

    public ReporteDtos.Resumen generarResumen(Authentication authentication) {
        String usuario = authentication != null ? authentication.getName() : "anonimo";
        return new ReporteDtos.Resumen(
                alumnoRepository.count(),
                docenteRepository.count(),
                materiaRepository.count(),
                cursoRepository.count(),
                alertaBajoRendimientoRepository.countByEstadoNot(EstadoAlerta.RESUELTA),
                solicitudRevisionRepository.countByEstadoIn(List.of(
                        EstadoSolicitud.PENDIENTE,
                        EstadoSolicitud.EN_REVISION_INSTITUCIONAL,
                        EstadoSolicitud.EN_APELACION_DISTRITAL)),
                usuario
        );
    }
}
