package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.EvaluacionPsicopedagogica;
import com.itsqmet.aplicativoweb.service.EvaluacionPsicopedagogicaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * NUEVO. Se expone deliberadamente bajo "/api/alumnos/{alumnoId}/..." y
 * nunca como parte del payload general de Alumno, dado el carácter
 * confidencial de esta información (Art. 29 del Acuerdo Ministerial).
 */
@RestController
@RequestMapping("/api/alumnos/{alumnoId}/evaluaciones-psicopedagogicas")
public class EvaluacionPsicopedagogicaController {

    private final EvaluacionPsicopedagogicaService evaluacionPsicopedagogicaService;

    public EvaluacionPsicopedagogicaController(EvaluacionPsicopedagogicaService evaluacionPsicopedagogicaService) {
        this.evaluacionPsicopedagogicaService = evaluacionPsicopedagogicaService;
    }

    @GetMapping
    public List<EvaluacionPsicopedagogica> listar(@PathVariable Long alumnoId) {
        return evaluacionPsicopedagogicaService.listarPorAlumno(alumnoId);
    }

    @GetMapping("/{id}")
    public EvaluacionPsicopedagogica obtenerPorId(@PathVariable Long alumnoId, @PathVariable Long id) {
        return evaluacionPsicopedagogicaService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<EvaluacionPsicopedagogica> registrar(@PathVariable Long alumnoId,
                                                                 @Valid @RequestBody EvaluacionPsicopedagogica evaluacion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evaluacionPsicopedagogicaService.registrar(evaluacion));
    }
}
