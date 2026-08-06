package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.dto.MejoraDtos;
import com.itsqmet.aplicativoweb.model.Nota;
import com.itsqmet.aplicativoweb.service.MejoraCalificacionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/alumnos/{alumnoId}/mejoras")
public class MejoraCalificacionController {

    private final MejoraCalificacionService mejoraCalificacionService;

    public MejoraCalificacionController(MejoraCalificacionService mejoraCalificacionService) {
        this.mejoraCalificacionService = mejoraCalificacionService;
    }

    @GetMapping("/cupos")
    public MejoraDtos.CupoMejora cupos(@PathVariable Long alumnoId,
                                        @RequestParam Long materiaCursoId,
                                        @RequestParam Long periodo) {
        return mejoraCalificacionService.obtenerCupos(alumnoId, materiaCursoId, periodo);
    }

    @PostMapping("/directa")
    public Nota mejoraDirecta(@PathVariable Long alumnoId,
                               @RequestParam Long notaOriginalId,
                               @Valid @RequestBody Nota evaluacionMejora) {
        return mejoraCalificacionService.solicitarMejoraDirecta(notaOriginalId, evaluacionMejora);
    }

    @PostMapping("/con-refuerzo")
    public Nota mejoraConRefuerzo(@PathVariable Long alumnoId,
                                   @RequestParam Long notaOriginalId,
                                   @RequestParam Long refuerzoPedagogicoId,
                                   @RequestParam BigDecimal calificacionRefuerzo,
                                   @Valid @RequestBody Nota evaluacionAdicional) {
        return mejoraCalificacionService.solicitarMejoraConRefuerzo(
                notaOriginalId, refuerzoPedagogicoId, calificacionRefuerzo, evaluacionAdicional);
    }
}
