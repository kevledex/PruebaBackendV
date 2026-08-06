package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.enums.Sostenimiento;
import com.itsqmet.aplicativoweb.enums.TipoOrganizacionPeriodo;
import com.itsqmet.aplicativoweb.exception.OperacionNoPermitidaException;
import com.itsqmet.aplicativoweb.exception.RecursoNoEncontradoException;
import com.itsqmet.aplicativoweb.model.Curso;
import com.itsqmet.aplicativoweb.model.Docente;
import com.itsqmet.aplicativoweb.repository.CursoRepository;
import com.itsqmet.aplicativoweb.repository.DocenteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * NUEVO. SecurityConfig ya protegía "/api/cursos/**" para ROLE_ADMIN, pero
 * ni la entidad Curso ni este servicio existían. Aplica la regla del
 * Cap. 2 (pág. 5) del Instructivo: el sostenimiento fiscal es
 * obligatoriamente TRIMESTRAL.
 */
@Service
public class CursoService {

    private final CursoRepository cursoRepository;
    private final DocenteRepository docenteRepository;

    public CursoService(CursoRepository cursoRepository, DocenteRepository docenteRepository) {
        this.cursoRepository = cursoRepository;
        this.docenteRepository = docenteRepository;
    }

    public List<Curso> listar() {
        return cursoRepository.findAll();
    }

    public List<Curso> listarPorAnioLectivo(Long anioLectivoId) {
        return cursoRepository.findByAnioLectivoId(anioLectivoId);
    }

    public Curso obtenerPorId(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Curso", id));
    }

    public Curso crear(Curso curso) {
        validarOrganizacionFiscal(curso);
        validarUnicidad(curso, null);
        curso.setTutor(resolverTutor(curso.getTutor()));
        return cursoRepository.save(curso);
    }

    public Curso actualizar(Long id, Curso datos) {
        Curso actual = obtenerPorId(id);
        validarOrganizacionFiscal(datos);
        validarUnicidad(datos, id);
        actual.setNivel(datos.getNivel());
        actual.setGrado(datos.getGrado());
        actual.setParalelo(datos.getParalelo());
        actual.setSostenimiento(datos.getSostenimiento());
        actual.setTipoOrganizacion(datos.getTipoOrganizacion());
        actual.setAnioLectivo(datos.getAnioLectivo());
        actual.setTutor(resolverTutor(datos.getTutor()));
        return cursoRepository.save(actual);
    }

    private Docente resolverTutor(Docente tutor) {
        if (tutor == null || tutor.getId() == null) {
            return null;
        }
        return docenteRepository.findById(tutor.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Docente", tutor.getId()));
    }

    public void eliminar(Long id) {
        if (!cursoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Curso", id);
        }
        cursoRepository.deleteById(id);
    }

    private void validarOrganizacionFiscal(Curso curso) {
        if (curso.getSostenimiento() == Sostenimiento.FISCAL
                && curso.getTipoOrganizacion() != TipoOrganizacionPeriodo.TRIMESTRAL) {
            throw new OperacionNoPermitidaException(
                    "Las instituciones de sostenimiento fiscal deben organizarse de forma TRIMESTRAL (Instructivo, pág. 5)");
        }
    }

    private void validarUnicidad(Curso curso, Long idAExcluir) {
        boolean existe = cursoRepository.existsByGradoAndParaleloAndAnioLectivoId(
                curso.getGrado(), curso.getParalelo(), curso.getAnioLectivo().getId());
        if (existe && idAExcluir == null) {
            throw new OperacionNoPermitidaException(
                    "Ya existe el curso " + curso.getGrado() + " '" + curso.getParalelo() + "' en ese año lectivo");
        }
    }
}
