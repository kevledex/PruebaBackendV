package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.exception.OperacionNoPermitidaException;
import com.itsqmet.aplicativoweb.exception.RecursoNoEncontradoException;
import com.itsqmet.aplicativoweb.model.Curso;
import com.itsqmet.aplicativoweb.model.PeriodoAcademico;
import com.itsqmet.aplicativoweb.repository.PeriodoAcademicoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * NUEVO. Genera y administra los periodos académicos (trimestres/quimestres)
 * de un Curso (Art. 4.b del Acuerdo Ministerial). "cerrarPeriodo" bloquea
 * nuevas Actividad/Nota sobre ese periodo (ver ActividadService); el
 * recálculo de promedios tras el cierre se dispara explícitamente desde
 * PromedioController para no acoplar este servicio con PromedioService.
 */
@Service
public class PeriodoAcademicoService {

    private final PeriodoAcademicoRepository periodoAcademicoRepository;
    private final CursoService cursoService;

    public PeriodoAcademicoService(PeriodoAcademicoRepository periodoAcademicoRepository, CursoService cursoService) {
        this.periodoAcademicoRepository = periodoAcademicoRepository;
        this.cursoService = cursoService;
    }

    public List<PeriodoAcademico> listarPorCurso(Long cursoId) {
        return periodoAcademicoRepository.findByCursoIdOrderByNumeroAsc(cursoId);
    }

    public PeriodoAcademico obtenerPorId(Long id) {
        return periodoAcademicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Periodo académico", id));
    }

    /**
     * Genera automáticamente los periodos del curso repartiendo en partes
     * iguales el rango de fechas de su año lectivo, según la cantidad que
     * corresponda a su tipo de organización.
     */
    public List<PeriodoAcademico> generarPeriodos(Long cursoId) {
        Curso curso = cursoService.obtenerPorId(cursoId);
        if (periodoAcademicoRepository.existsByCursoId(cursoId)) {
            throw new OperacionNoPermitidaException("El curso ya tiene periodos académicos generados");
        }

        int cantidad = switch (curso.getTipoOrganizacion()) {
            case TRIMESTRAL -> 3;
            case QUIMESTRAL -> 2;
            case CUATRIMESTRAL -> 2;
            case BIMESTRAL -> 4;
        };

        LocalDate inicioAnio = curso.getAnioLectivo().getFechaInicio();
        LocalDate finAnio = curso.getAnioLectivo().getFechaFin();
        long diasTotales = ChronoUnit.DAYS.between(inicioAnio, finAnio);
        long diasPorPeriodo = diasTotales / cantidad;

        List<PeriodoAcademico> periodos = new ArrayList<>();
        LocalDate cursorInicio = inicioAnio;
        for (int numero = 1; numero <= cantidad; numero++) {
            LocalDate cursorFin = (numero == cantidad) ? finAnio : cursorInicio.plusDays(diasPorPeriodo);
            PeriodoAcademico periodo = new PeriodoAcademico();
            periodo.setCurso(curso);
            periodo.setNumero(numero);
            periodo.setFechaInicio(cursorInicio);
            periodo.setFechaFin(cursorFin);
            periodo.setCerrado(false);
            periodos.add(periodoAcademicoRepository.save(periodo));
            cursorInicio = cursorFin.plusDays(1);
        }
        return periodos;
    }

    public PeriodoAcademico cerrarPeriodo(Long id) {
        PeriodoAcademico periodo = obtenerPorId(id);
        periodo.setCerrado(true);
        return periodoAcademicoRepository.save(periodo);
    }
}
