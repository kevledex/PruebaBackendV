package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.dto.DocenteDtos;
import com.itsqmet.aplicativoweb.exception.RecursoNoEncontradoException;
import com.itsqmet.aplicativoweb.model.Docente;
import com.itsqmet.aplicativoweb.model.Materia;
import com.itsqmet.aplicativoweb.model.MateriaCurso;
import com.itsqmet.aplicativoweb.repository.DocenteRepository;
import com.itsqmet.aplicativoweb.repository.MateriaCursoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * CORREGIDO/AMPLIADO: DocenteController ya invocaba
 * "docenteService.asignarMaterias(id, solicitud)", pero el método no
 * existía en esta clase (por lo tanto el controlador no compilaba). Se
 * implementa contra la nueva entidad MateriaCurso (ver paquete
 * "estructura académica"), que es donde ahora vive la relación real
 * materia-curso-docente.
 */
@Service
public class DocenteService {

    private final DocenteRepository docenteRepository;
    private final MateriaCursoRepository materiaCursoRepository;

    public DocenteService(DocenteRepository docenteRepository, MateriaCursoRepository materiaCursoRepository) {
        this.docenteRepository = docenteRepository;
        this.materiaCursoRepository = materiaCursoRepository;
    }

    public List<Docente> obtenerTodos() {
        return docenteRepository.findAll();
    }

    public Optional<Docente> obtenerPorId(Long id) {
        return docenteRepository.findById(id);
    }

    public Docente guardarDocente(Docente docente) {
        return docenteRepository.save(docente);
    }

    public void eliminarDocente(Long id) {
        docenteRepository.deleteById(id);
    }

    public List<Materia> asignarMaterias(Long docenteId, DocenteDtos.AsignacionMaterias solicitud) {
        Docente docente = docenteRepository.findById(docenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Docente", docenteId));

        List<MateriaCurso> materiasCurso = materiaCursoRepository.findAllById(solicitud.materiaCursoIds());
        if (materiasCurso.size() != solicitud.materiaCursoIds().size()) {
            throw new RecursoNoEncontradoException("Una o más MateriaCurso indicadas no existen");
        }

        materiasCurso.forEach(materiaCurso -> materiaCurso.setDocente(docente));
        materiaCursoRepository.saveAll(materiasCurso);

        return materiasCurso.stream().map(MateriaCurso::getMateria).toList();
    }
}
