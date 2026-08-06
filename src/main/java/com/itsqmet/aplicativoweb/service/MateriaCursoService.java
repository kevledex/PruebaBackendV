package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.exception.OperacionNoPermitidaException;
import com.itsqmet.aplicativoweb.exception.RecursoNoEncontradoException;
import com.itsqmet.aplicativoweb.model.Materia;
import com.itsqmet.aplicativoweb.model.MateriaCurso;
import com.itsqmet.aplicativoweb.repository.MateriaCursoRepository;
import com.itsqmet.aplicativoweb.repository.MateriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * NUEVO. Administra la oferta de materias por curso (docente asignado y
 * periodos pedagógicos semanales). "aportesRequeridos" aplica la Tabla 35
 * del Instructivo (pág. 47): 1-3 periodos/semana -> 3 aportes mínimos;
 * 4-5 -> 5 aportes; 6 o más -> 9 aportes.
 */
@Service
public class MateriaCursoService {

    private final MateriaCursoRepository materiaCursoRepository;
    private final MateriaRepository materiaRepository;

    public MateriaCursoService(MateriaCursoRepository materiaCursoRepository, MateriaRepository materiaRepository) {
        this.materiaCursoRepository = materiaCursoRepository;
        this.materiaRepository = materiaRepository;
    }

    public List<MateriaCurso> listarPorCurso(Long cursoId) {
        return materiaCursoRepository.findByCursoId(cursoId);
    }

    public MateriaCurso obtenerPorId(Long id) {
        return materiaCursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("MateriaCurso", id));
    }

    public MateriaCurso crear(MateriaCurso materiaCurso) {
        Materia materia = materiaRepository.findById(materiaCurso.getMateria().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Materia", materiaCurso.getMateria().getId()));
        materiaCurso.setMateria(materia);

        if (materiaCursoRepository.existsByMateriaIdAndCursoId(
                materiaCurso.getMateria().getId(), materiaCurso.getCurso().getId())) {
            throw new OperacionNoPermitidaException("Esa materia ya está ofertada en ese curso");
        }
        return materiaCursoRepository.save(materiaCurso);
    }

    public MateriaCurso actualizar(Long id, MateriaCurso datos) {
        MateriaCurso actual = obtenerPorId(id);
        actual.setPeriodosPedagogicosSemana(datos.getPeriodosPedagogicosSemana());
        if (datos.getDocente() != null) {
            actual.setDocente(datos.getDocente());
        }
        return materiaCursoRepository.save(actual);
    }

    public void eliminar(Long id) {
        if (!materiaCursoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("MateriaCurso", id);
        }
        materiaCursoRepository.deleteById(id);
    }

    public int aportesRequeridos(Long id) {
        MateriaCurso materiaCurso = obtenerPorId(id);
        int periodos = materiaCurso.getPeriodosPedagogicosSemana();
        if (periodos <= 3) {
            return 3;
        }
        if (periodos <= 5) {
            return 5;
        }
        return 9;
    }
}
