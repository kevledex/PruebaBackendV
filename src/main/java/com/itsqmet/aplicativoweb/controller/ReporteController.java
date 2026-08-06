package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.dto.ReporteDtos.Resumen;
import com.itsqmet.aplicativoweb.service.ReporteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {
    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService){
        this.reporteService = reporteService;
    }

    @GetMapping("/resumen")
    public Resumen resumen(Authentication authentication){
        return reporteService.generarResumen(authentication);
    }
}
