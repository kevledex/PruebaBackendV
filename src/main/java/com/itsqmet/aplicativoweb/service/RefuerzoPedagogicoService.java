package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.enums.EstadoRefuerzo;
import com.itsqmet.aplicativoweb.enums.OrigenRefuerzo;
import com.itsqmet.aplicativoweb.exception.OperacionNoPermitidaException;
import com.itsqmet.aplicativoweb.exception.RecursoNoEncontradoException;
import com.itsqmet.aplicativoweb.model.RefuerzoPedagogico;
import com.itsqmet.aplicativoweb.model.Usuario;
import com.itsqmet.aplicativoweb.repository.RefuerzoPedagogicoRepository;
import com.itsqmet.aplicativoweb.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * NUEVO. Administra el ciclo de vida del refuerzo pedagógico (Art. 3.j del
 * Acuerdo). Un refuerzo debe pasar por APROBADO (autoridad institucional)
 * antes de ejecutarse (pág. 36 del Instructivo), y por FINALIZADO antes de
 * habilitar la mejora con refuerzo o la supletoria que lo originaron (ver
 * MejoraCalificacionService y SupletoriaService).
 */
@Service
public class RefuerzoPedagogicoService {

    private final RefuerzoPedagogicoRepository refuerzoPedagogicoRepository;
    private final UsuarioRepository usuarioRepository;

    public RefuerzoPedagogicoService(RefuerzoPedagogicoRepository refuerzoPedagogicoRepository,
                                      UsuarioRepository usuarioRepository) {
        this.refuerzoPedagogicoRepository = refuerzoPedagogicoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<RefuerzoPedagogico> listarPorAlumno(Long alumnoId) {
        return refuerzoPedagogicoRepository.findByAlumnoId(alumnoId);
    }

    public RefuerzoPedagogico obtenerPorId(Long id) {
        return refuerzoPedagogicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("RefuerzoPedagogico", id));
    }

    public RefuerzoPedagogico planificar(RefuerzoPedagogico refuerzo) {
        validarVentana(refuerzo);
        refuerzo.setEstado(EstadoRefuerzo.PLANIFICADO);
        return refuerzoPedagogicoRepository.save(refuerzo);
    }

    public RefuerzoPedagogico aprobar(Long id, Authentication authentication) {
        RefuerzoPedagogico refuerzo = obtenerPorId(id);
        if (refuerzo.getEstado() != EstadoRefuerzo.PLANIFICADO) {
            throw new OperacionNoPermitidaException("Solo se puede aprobar un refuerzo en estado PLANIFICADO");
        }
        Usuario autoridad = usuarioRepository.findByUsuarioIgnoreCase(authentication.getName())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario autenticado no encontrado"));
        refuerzo.setEstado(EstadoRefuerzo.APROBADO);
        refuerzo.setFechaAprobacion(LocalDate.now());
        refuerzo.setAprobadoPor(autoridad);
        return refuerzoPedagogicoRepository.save(refuerzo);
    }

    public RefuerzoPedagogico iniciarEjecucion(Long id) {
        RefuerzoPedagogico refuerzo = obtenerPorId(id);
        if (refuerzo.getEstado() != EstadoRefuerzo.APROBADO) {
            throw new OperacionNoPermitidaException("El refuerzo debe estar APROBADO antes de iniciar su ejecución");
        }
        refuerzo.setEstado(EstadoRefuerzo.EN_EJECUCION);
        return refuerzoPedagogicoRepository.save(refuerzo);
    }

    public RefuerzoPedagogico finalizar(Long id) {
        RefuerzoPedagogico refuerzo = obtenerPorId(id);
        refuerzo.setEstado(EstadoRefuerzo.FINALIZADO);
        return refuerzoPedagogicoRepository.save(refuerzo);
    }

    private void validarVentana(RefuerzoPedagogico refuerzo) {
        boolean requiereMaximoCincoDias = refuerzo.getOrigen() == OrigenRefuerzo.SUPLETORIA
                || refuerzo.getOrigen() == OrigenRefuerzo.MEJORA_CALIFICACION;
        if (requiereMaximoCincoDias
                && ChronoUnit.DAYS.between(refuerzo.getFechaInicio(), refuerzo.getFechaFin()) > 5) {
            throw new OperacionNoPermitidaException(
                    "El refuerzo pedagógico previo a una mejora con refuerzo o a una supletoria no puede durar "
                            + "más de 5 días (Art. 12 del Acuerdo Ministerial; pág. 36 del Instructivo)");
        }
    }
}
