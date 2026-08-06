package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.EvaluacionDestreza;
import com.itsqmet.aplicativoweb.repository.EvaluacionDestrezaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * NUEVO. CRUD estándar (heredado de BaseCrudController, igual que
 * DocenteController/RolController) más una consulta filtrada por alumno y
 * periodo, útil para pintar el informe de progreso de Inicial/Preparatoria
 * (Tablas 24-27 del Instructivo).
 */
@RestController
@RequestMapping("/api/evaluaciones-destreza")
public class EvaluacionDestrezaController extends BaseCrudController<EvaluacionDestreza> {

    private final EvaluacionDestrezaRepository evaluacionDestrezaRepository;

    public EvaluacionDestrezaController(EvaluacionDestrezaRepository evaluacionDestrezaRepository) {
        super(evaluacionDestrezaRepository);
        this.evaluacionDestrezaRepository = evaluacionDestrezaRepository;
    }

    @GetMapping("/alumno/{alumnoId}/periodo/{periodoId}")
    public List<EvaluacionDestreza> porAlumnoYPeriodo(@PathVariable Long alumnoId, @PathVariable Long periodoId) {
        return evaluacionDestrezaRepository.findByAlumnoIdAndPeriodoAcademicoId(alumnoId, periodoId);
    }
}
