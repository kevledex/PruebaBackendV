package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.EvaluacionDiagnosticaSocioemocional;
import com.itsqmet.aplicativoweb.repository.EvaluacionDiagnosticaSocioemocionalRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alumnos/{alumnoId}/evaluacion-diagnostica-socioemocional")
public class EvaluacionDiagnosticaSocioemocionalController {

    private final EvaluacionDiagnosticaSocioemocionalRepository evaluacionDiagnosticaSocioemocionalRepository;

    public EvaluacionDiagnosticaSocioemocionalController(EvaluacionDiagnosticaSocioemocionalRepository evaluacionDiagnosticaSocioemocionalRepository) {
        this.evaluacionDiagnosticaSocioemocionalRepository = evaluacionDiagnosticaSocioemocionalRepository;
    }

    @GetMapping
    public List<EvaluacionDiagnosticaSocioemocional> listar(@PathVariable Long alumnoId) {
        return evaluacionDiagnosticaSocioemocionalRepository.findByAlumnoId(alumnoId);
    }

    @PostMapping
    public ResponseEntity<EvaluacionDiagnosticaSocioemocional> registrar(
            @PathVariable Long alumnoId, @Valid @RequestBody EvaluacionDiagnosticaSocioemocional evaluacion) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(evaluacionDiagnosticaSocioemocionalRepository.save(evaluacion));
    }
}
