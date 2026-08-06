package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.Nota;
import com.itsqmet.aplicativoweb.service.SupletoriaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/alumnos/{alumnoId}/supletorias")
public class SupletoriaController {

    private final SupletoriaService supletoriaService;

    public SupletoriaController(SupletoriaService supletoriaService) {
        this.supletoriaService = supletoriaService;
    }

    @GetMapping("/elegibilidad")
    public Map<String, Boolean> elegibilidad(@PathVariable Long alumnoId, @RequestParam Long materiaCursoId) {
        return Map.of("elegible", supletoriaService.esElegible(alumnoId, materiaCursoId));
    }

    @PostMapping
    public Nota registrar(@PathVariable Long alumnoId,
                           @RequestParam Long refuerzoPedagogicoId,
                           @Valid @RequestBody Nota notaSupletoria) {
        return supletoriaService.registrarSupletoria(alumnoId, refuerzoPedagogicoId, notaSupletoria);
    }
}
