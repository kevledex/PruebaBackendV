package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.PromedioMateriaPeriodo;
import com.itsqmet.aplicativoweb.service.PromedioService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
public class PromedioController {

    private final PromedioService promedioService;

    public PromedioController(PromedioService promedioService) {
        this.promedioService = promedioService;
    }

    @GetMapping("/api/alumnos/{alumnoId}/materias/{materiaCursoId}/promedio")
    public PromedioMateriaPeriodo promedioPeriodo(@PathVariable Long alumnoId,
                                                    @PathVariable Long materiaCursoId,
                                                    @RequestParam Long periodo) {
        return promedioService.calcularPromedioPeriodo(alumnoId, materiaCursoId, periodo);
    }

    @GetMapping("/api/alumnos/{alumnoId}/materias/{materiaCursoId}/promedio-anual")
    public Map<String, BigDecimal> promedioAnual(@PathVariable Long alumnoId, @PathVariable Long materiaCursoId) {
        return Map.of("promedioAnual", promedioService.calcularPromedioAnual(alumnoId, materiaCursoId));
    }

    @PostMapping("/api/periodos/{periodoId}/recalcular-promedios")
    public List<PromedioMateriaPeriodo> recalcular(@PathVariable Long periodoId) {
        return promedioService.recalcularPeriodo(periodoId);
    }
}
