package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.PeriodoAcademico;
import com.itsqmet.aplicativoweb.service.PeriodoAcademicoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/periodos")
public class PeriodoAcademicoController {

    private final PeriodoAcademicoService periodoAcademicoService;

    public PeriodoAcademicoController(PeriodoAcademicoService periodoAcademicoService) {
        this.periodoAcademicoService = periodoAcademicoService;
    }

    @GetMapping("/{id}")
    public PeriodoAcademico obtenerPorId(@PathVariable Long id) {
        return periodoAcademicoService.obtenerPorId(id);
    }

    @PatchMapping("/{id}/cerrar")
    public PeriodoAcademico cerrar(@PathVariable Long id) {
        return periodoAcademicoService.cerrarPeriodo(id);
    }
}
