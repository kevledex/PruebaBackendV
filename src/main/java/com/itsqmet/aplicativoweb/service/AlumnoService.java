package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.exception.AlumnoNoEncontradoException;
import com.itsqmet.aplicativoweb.exception.DatosInvalidosException;
import com.itsqmet.aplicativoweb.exception.RecursoNoEncontradoException;
import com.itsqmet.aplicativoweb.model.Alumno;
import com.itsqmet.aplicativoweb.model.Curso;
import com.itsqmet.aplicativoweb.model.Representante;
import com.itsqmet.aplicativoweb.repository.AlumnoRepository;
import com.itsqmet.aplicativoweb.repository.CursoRepository;
import com.itsqmet.aplicativoweb.repository.RepresentanteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AJUSTADO: "actualizar" tenía la línea "alumno.setCurso(...)" comentada
 * porque el campo Curso no existía todavía en Alumno. Ahora que sí existe
 * (ver ajustes al modelo), se habilita esa línea y se valida su presencia
 * al crear. También se sincronizan los campos de necesidades educativas
 * específicas (Cap. X del Acuerdo) al actualizar.
 *
 * CORREGIDO: "curso"/"representanteRegistro" llegan desde el frontend como
 * un objeto parcial ({id}) sin el resto de sus campos. Guardar ese objeto
 * "tal cual" dependía de que Hibernate lo tratara como una referencia
 * detached por tener id no nulo, lo que en la práctica podía fallar según
 * el estado de la sesión y terminaba en un 500 genérico. Ahora se resuelven
 * explícitamente contra sus repositorios antes de guardar.
 */
@Service
public class AlumnoService {

    private final AlumnoRepository alumnoRepository;
    private final CursoRepository cursoRepository;
    private final RepresentanteRepository representanteRepository;

    public AlumnoService(AlumnoRepository alumnoRepository, CursoRepository cursoRepository,
                          RepresentanteRepository representanteRepository) {
        this.alumnoRepository = alumnoRepository;
        this.cursoRepository = cursoRepository;
        this.representanteRepository = representanteRepository;
    }

    // READ - listar todos

    public List<Alumno> obtenerTodo() {
        return alumnoRepository.findAll();
    }

    // READ - buscar por id con excepción personalizada

    public Alumno buscarPorId(Long id) {
        return alumnoRepository.findById(id)
                .orElseThrow(() -> new AlumnoNoEncontradoException(id));
    }

    // CREATE - crear alumno con validación básica

    public Alumno crearAlumno(Alumno alumno) {
        if (alumno.getNombres() == null || alumno.getApellidos() == null) {
            throw new DatosInvalidosException("Nombre y Apellido son obligatorios");
        }
        if (alumno.getCurso() == null) {
            throw new DatosInvalidosException("El curso es obligatorio");
        }
        alumno.setCurso(resolverCurso(alumno.getCurso().getId()));
        alumno.setRepresentanteRegistro(resolverRepresentante(alumno.getRepresentanteRegistro()));
        return alumnoRepository.save(alumno);
    }

    // UPDATE - actualizar alumno

    public Alumno actualizar(Long id, Alumno alumnoActualizado) {
        return alumnoRepository.findById(id).map(alumno -> {
            alumno.setNombres(alumnoActualizado.getNombres());
            alumno.setApellidos(alumnoActualizado.getApellidos());
            alumno.setCurso(resolverCurso(alumnoActualizado.getCurso().getId()));
            alumno.setRepresentanteRegistro(resolverRepresentante(alumnoActualizado.getRepresentanteRegistro()));
            alumno.setTelefono(alumnoActualizado.getTelefono());
            alumno.setTieneNecesidadesEducativasEspecificas(alumnoActualizado.isTieneNecesidadesEducativasEspecificas());
            alumno.setTipoAdaptacion(alumnoActualizado.getTipoAdaptacion());
            return alumnoRepository.save(alumno);
        }).orElseThrow(() -> new AlumnoNoEncontradoException(id));
    }

    private Curso resolverCurso(Long cursoId) {
        return cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Curso", cursoId));
    }

    private Representante resolverRepresentante(Representante representante) {
        if (representante == null || representante.getId() == null) {
            return null;
        }
        return representanteRepository.findById(representante.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Representante", representante.getId()));
    }

    // DELETE - eliminar alumno
    public boolean eliminar(Long id) {
        if (!alumnoRepository.existsById(id)) {
            throw new AlumnoNoEncontradoException(id);
        }
        alumnoRepository.deleteById(id);
        return true;
    }
}
