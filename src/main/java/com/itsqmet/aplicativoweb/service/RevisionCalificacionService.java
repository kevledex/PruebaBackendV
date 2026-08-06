package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.enums.EstadoSolicitud;
import com.itsqmet.aplicativoweb.exception.OperacionNoPermitidaException;
import com.itsqmet.aplicativoweb.exception.RecursoNoEncontradoException;
import com.itsqmet.aplicativoweb.model.Docente;
import com.itsqmet.aplicativoweb.model.Nota;
import com.itsqmet.aplicativoweb.model.Representante;
import com.itsqmet.aplicativoweb.model.SolicitudRevision;
import com.itsqmet.aplicativoweb.repository.DocenteRepository;
import com.itsqmet.aplicativoweb.repository.NotaRepository;
import com.itsqmet.aplicativoweb.repository.RepresentanteRepository;
import com.itsqmet.aplicativoweb.repository.SolicitudRevisionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * NUEVO. Implementa el flujo de revisión y apelación de calificaciones
 * (Art. 40 del RGLOEI; Cap. 7 del Instructivo, pág. 37-40), que antes no
 * existía en absoluto:
 *  - Plazo de 15 días calendario desde la notificación de la calificación
 *    para solicitar la revisión.
 *  - La comisión de rectificación NO puede incluir al docente que otorgó
 *    la calificación original.
 *  - Plazo de 3 días (hábiles, simplificado aquí a días corridos) para
 *    resolver.
 *  - Segunda instancia (apelación distrital): resolución definitiva, sin
 *    posibilidad de una nueva apelación.
 */
@Service
public class RevisionCalificacionService {

    private final SolicitudRevisionRepository solicitudRevisionRepository;
    private final NotaRepository notaRepository;
    private final RepresentanteRepository representanteRepository;
    private final DocenteRepository docenteRepository;

    public RevisionCalificacionService(SolicitudRevisionRepository solicitudRevisionRepository,
                                        NotaRepository notaRepository,
                                        RepresentanteRepository representanteRepository,
                                        DocenteRepository docenteRepository) {
        this.solicitudRevisionRepository = solicitudRevisionRepository;
        this.notaRepository = notaRepository;
        this.representanteRepository = representanteRepository;
        this.docenteRepository = docenteRepository;
    }

    public SolicitudRevision solicitar(Long notaId, Long representanteId, String motivo) {
        Nota nota = notaRepository.findById(notaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Nota", notaId));
        Representante representante = representanteRepository.findById(representanteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Representante", representanteId));

        if (ChronoUnit.DAYS.between(nota.getFecha(), LocalDate.now()) > 15) {
            throw new OperacionNoPermitidaException(
                    "El plazo de 15 días calendario para solicitar la revisión de esta calificación ya venció (Art. 40 del RGLOEI)");
        }

        SolicitudRevision solicitud = new SolicitudRevision();
        solicitud.setNotaCuestionada(nota);
        solicitud.setSolicitante(representante);
        solicitud.setFechaSolicitud(LocalDate.now());
        solicitud.setMotivo(motivo);
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        return solicitudRevisionRepository.save(solicitud);
    }

    public SolicitudRevision asignarComision(Long solicitudId, List<Long> docenteIds) {
        SolicitudRevision solicitud = obtenerPorId(solicitudId);
        List<Docente> comision = docenteRepository.findAllById(docenteIds);

        Docente docenteCalificador = solicitud.getNotaCuestionada().getActividad().getMateriaCurso().getDocente();
        boolean incluyeAlCalificador = docenteCalificador != null && comision.stream()
                .anyMatch(docente -> docente.getId().equals(docenteCalificador.getId()));
        if (incluyeAlCalificador) {
            throw new OperacionNoPermitidaException(
                    "La comisión de rectificación no puede incluir al docente que otorgó la calificación original (Cap. 7 del Instructivo)");
        }

        solicitud.setComisionRectificacion(comision);
        solicitud.setEstado(EstadoSolicitud.EN_REVISION_INSTITUCIONAL);
        solicitud.setFechaLimiteResolucion(LocalDate.now().plusDays(3));
        return solicitudRevisionRepository.save(solicitud);
    }

    public SolicitudRevision resolver(Long solicitudId, boolean rectificar, BigDecimal nuevaCalificacion, String informe) {
        SolicitudRevision solicitud = obtenerPorId(solicitudId);
        solicitud.setInformeResultado(informe);
        if (rectificar) {
            Nota nota = solicitud.getNotaCuestionada();
            nota.setCalificacion(NotaService.truncar2Decimales(nuevaCalificacion));
            notaRepository.save(nota);
            solicitud.setEstado(EstadoSolicitud.RESUELTA_RECTIFICADA);
        } else {
            solicitud.setEstado(EstadoSolicitud.RESUELTA_RATIFICADA);
        }
        return solicitudRevisionRepository.save(solicitud);
    }

    public SolicitudRevision marcarNuevaEvaluacionRequerida(Long solicitudId, String informe) {
        SolicitudRevision solicitud = obtenerPorId(solicitudId);
        solicitud.setInformeResultado(informe);
        solicitud.setEstado(EstadoSolicitud.NUEVA_EVALUACION_REQUERIDA);
        return solicitudRevisionRepository.save(solicitud);
    }

    public SolicitudRevision apelar(Long solicitudId) {
        SolicitudRevision solicitud = obtenerPorId(solicitudId);
        if (solicitud.getEstado() != EstadoSolicitud.RESUELTA_RATIFICADA
                && solicitud.getEstado() != EstadoSolicitud.RESUELTA_RECTIFICADA) {
            throw new OperacionNoPermitidaException("Solo se puede apelar una solicitud ya resuelta en primera instancia");
        }
        solicitud.setApelada(true);
        solicitud.setFechaApelacion(LocalDate.now());
        solicitud.setEstado(EstadoSolicitud.EN_APELACION_DISTRITAL);
        return solicitudRevisionRepository.save(solicitud);
    }

    public SolicitudRevision resolverApelacion(Long solicitudId, List<Long> comisionApelacionIds, String resolucion) {
        SolicitudRevision solicitud = obtenerPorId(solicitudId);
        solicitud.setComisionApelacion(docenteRepository.findAllById(comisionApelacionIds));
        solicitud.setResolucionApelacion(resolucion);
        // Cap. 7.2 del Instructivo: la resolución de apelación es definitiva, sin oportunidad a nueva recalificación.
        solicitud.setEstado(EstadoSolicitud.RESUELTA_DEFINITIVA);
        return solicitudRevisionRepository.save(solicitud);
    }

    public SolicitudRevision obtenerPorId(Long id) {
        return solicitudRevisionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("SolicitudRevision", id));
    }
}
