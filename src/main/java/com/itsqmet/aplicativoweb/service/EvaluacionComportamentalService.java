package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.model.EvaluacionComportamental;
import com.itsqmet.aplicativoweb.repository.EvaluacionComportamentalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * NUEVO. Cap. IX (Art. 26-28) del Acuerdo Ministerial. Hace "upsert" por
 * alumno+periodo (un solo registro de comportamiento por periodo, tal como
 * se muestra en las boletas de ejemplo del Instructivo) y fuerza
 * "cuentaParaPromocion=false" sin importar lo que llegue en el payload.
 */
@Service
public class EvaluacionComportamentalService {

    private final EvaluacionComportamentalRepository evaluacionComportamentalRepository;

    public EvaluacionComportamentalService(EvaluacionComportamentalRepository evaluacionComportamentalRepository) {
        this.evaluacionComportamentalRepository = evaluacionComportamentalRepository;
    }

    public List<EvaluacionComportamental> listarPorAlumno(Long alumnoId) {
        return evaluacionComportamentalRepository.findByAlumnoId(alumnoId);
    }

    public EvaluacionComportamental registrar(EvaluacionComportamental evaluacion) {
        evaluacion.setCuentaParaPromocion(false);

        return evaluacionComportamentalRepository
                .findByAlumnoIdAndPeriodoAcademicoId(evaluacion.getAlumno().getId(), evaluacion.getPeriodoAcademico().getId())
                .map(existente -> {
                    existente.setDocente(evaluacion.getDocente());
                    existente.setHabilidadesObservadas(evaluacion.getHabilidadesObservadas());
                    existente.setDescripcionCualitativa(evaluacion.getDescripcionCualitativa());
                    return evaluacionComportamentalRepository.save(existente);
                })
                .orElseGet(() -> evaluacionComportamentalRepository.save(evaluacion));
    }
}
