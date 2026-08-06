package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.exception.RecursoNoEncontradoException;
import com.itsqmet.aplicativoweb.model.EvaluacionPsicopedagogica;
import com.itsqmet.aplicativoweb.repository.EvaluacionPsicopedagogicaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * NUEVO. Art. 29 del Acuerdo Ministerial. El control de acceso fino (solo
 * ADMIN, DECE y el/los docentes asignados al alumno deberían poder leerla)
 * se resuelve con los permisos de Rol/SecurityConfig; este servicio no debe
 * exponerse nunca desde el endpoint general de Alumno.
 */
@Service
public class EvaluacionPsicopedagogicaService {

    private final EvaluacionPsicopedagogicaRepository evaluacionPsicopedagogicaRepository;

    public EvaluacionPsicopedagogicaService(EvaluacionPsicopedagogicaRepository evaluacionPsicopedagogicaRepository) {
        this.evaluacionPsicopedagogicaRepository = evaluacionPsicopedagogicaRepository;
    }

    public List<EvaluacionPsicopedagogica> listarPorAlumno(Long alumnoId) {
        return evaluacionPsicopedagogicaRepository.findByAlumnoId(alumnoId);
    }

    public EvaluacionPsicopedagogica obtenerPorId(Long id) {
        return evaluacionPsicopedagogicaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("EvaluacionPsicopedagogica", id));
    }

    public EvaluacionPsicopedagogica registrar(EvaluacionPsicopedagogica evaluacion) {
        return evaluacionPsicopedagogicaRepository.save(evaluacion);
    }
}
