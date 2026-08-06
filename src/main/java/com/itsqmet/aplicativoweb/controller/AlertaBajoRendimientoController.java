package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.AlertaBajoRendimiento;
import com.itsqmet.aplicativoweb.service.AlertaBajoRendimientoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
public class AlertaBajoRendimientoController {

    private final AlertaBajoRendimientoService alertaBajoRendimientoService;

    public AlertaBajoRendimientoController(AlertaBajoRendimientoService alertaBajoRendimientoService) {
        this.alertaBajoRendimientoService = alertaBajoRendimientoService;
    }

    @GetMapping
    public List<AlertaBajoRendimiento> listar() {
        return alertaBajoRendimientoService.listar();
    }

    @GetMapping("/alumno/{alumnoId}")
    public List<AlertaBajoRendimiento> porAlumno(@PathVariable Long alumnoId) {
        return alertaBajoRendimientoService.listarPorAlumno(alumnoId);
    }

    @PostMapping("/detectar")
    public List<AlertaBajoRendimiento> detectar(@RequestParam Long periodo) {
        return alertaBajoRendimientoService.detectarAlertas(periodo);
    }

    @PatchMapping("/{id}/notificar")
    public AlertaBajoRendimiento notificar(@PathVariable Long id) {
        return alertaBajoRendimientoService.notificarRepresentante(id);
    }

    @PatchMapping("/{id}/iniciar-refuerzo")
    public AlertaBajoRendimiento iniciarRefuerzo(@PathVariable Long id, @RequestParam Long refuerzoId) {
        return alertaBajoRendimientoService.iniciarRefuerzo(id, refuerzoId);
    }

    @PatchMapping("/{id}/resolver")
    public AlertaBajoRendimiento resolver(@PathVariable Long id) {
        return alertaBajoRendimientoService.resolver(id);
    }

    @PatchMapping("/{id}/derivar-psicopedagogico")
    public AlertaBajoRendimiento derivarPsicopedagogico(@PathVariable Long id, @RequestParam Long evaluacionPsicopedagogicaId) {
        return alertaBajoRendimientoService.derivarPsicopedagogico(id, evaluacionPsicopedagogicaId);
    }
}
