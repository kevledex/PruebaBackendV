package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.EvaluacionComportamental;
import com.itsqmet.aplicativoweb.service.EvaluacionComportamentalService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alumnos/{alumnoId}/evaluacion-comportamental")
public class EvaluacionComportamentalController {

    private final EvaluacionComportamentalService evaluacionComportamentalService;

    public EvaluacionComportamentalController(EvaluacionComportamentalService evaluacionComportamentalService) {
        this.evaluacionComportamentalService = evaluacionComportamentalService;
    }

    @GetMapping
    public List<EvaluacionComportamental> listar(@PathVariable Long alumnoId) {
        return evaluacionComportamentalService.listarPorAlumno(alumnoId);
    }

    @PostMapping
    public EvaluacionComportamental registrar(@PathVariable Long alumnoId, @Valid @RequestBody EvaluacionComportamental evaluacion) {
        return evaluacionComportamentalService.registrar(evaluacion);
    }
}
