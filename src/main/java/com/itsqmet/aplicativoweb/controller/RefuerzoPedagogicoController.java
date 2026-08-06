package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.RefuerzoPedagogico;
import com.itsqmet.aplicativoweb.service.RefuerzoPedagogicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/refuerzos")
public class RefuerzoPedagogicoController {

    private final RefuerzoPedagogicoService refuerzoPedagogicoService;

    public RefuerzoPedagogicoController(RefuerzoPedagogicoService refuerzoPedagogicoService) {
        this.refuerzoPedagogicoService = refuerzoPedagogicoService;
    }

    @GetMapping("/alumno/{alumnoId}")
    public List<RefuerzoPedagogico> porAlumno(@PathVariable Long alumnoId) {
        return refuerzoPedagogicoService.listarPorAlumno(alumnoId);
    }

    @GetMapping("/{id}")
    public RefuerzoPedagogico obtenerPorId(@PathVariable Long id) {
        return refuerzoPedagogicoService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<RefuerzoPedagogico> planificar(@Valid @RequestBody RefuerzoPedagogico refuerzo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(refuerzoPedagogicoService.planificar(refuerzo));
    }

    @PatchMapping("/{id}/aprobar")
    public RefuerzoPedagogico aprobar(@PathVariable Long id, Authentication authentication) {
        return refuerzoPedagogicoService.aprobar(id, authentication);
    }

    @PatchMapping("/{id}/iniciar")
    public RefuerzoPedagogico iniciar(@PathVariable Long id) {
        return refuerzoPedagogicoService.iniciarEjecucion(id);
    }

    @PatchMapping("/{id}/finalizar")
    public RefuerzoPedagogico finalizar(@PathVariable Long id) {
        return refuerzoPedagogicoService.finalizar(id);
    }
}
