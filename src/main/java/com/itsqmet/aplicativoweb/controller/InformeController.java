package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.dto.BoletaDtos;
import com.itsqmet.aplicativoweb.service.BoletaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alumnos/{alumnoId}/informes")
public class InformeController {

    private final BoletaService boletaService;

    public InformeController(BoletaService boletaService) {
        this.boletaService = boletaService;
    }

    @GetMapping("/progreso")
    public BoletaDtos.InformeProgresoDto progreso(@PathVariable Long alumnoId, @RequestParam Long periodo) {
        return boletaService.generarInformeProgreso(alumnoId, periodo);
    }

    @GetMapping("/final-anual")
    public BoletaDtos.InformeFinalAnualDto finalAnual(@PathVariable Long alumnoId, @RequestParam Long anioLectivo) {
        return boletaService.generarInformeFinalAnual(alumnoId, anioLectivo);
    }
}
