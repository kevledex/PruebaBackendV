package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.RegistroPromocion;
import com.itsqmet.aplicativoweb.service.PromocionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/promociones")
public class PromocionController {

    private final PromocionService promocionService;

    public PromocionController(PromocionService promocionService) {
        this.promocionService = promocionService;
    }

    @PostMapping("/calcular")
    public RegistroPromocion calcular(@RequestParam Long alumnoId, @RequestParam Long anioLectivoId) {
        return promocionService.calcularPromocion(alumnoId, anioLectivoId);
    }

    @PostMapping("/recalcular-tras-supletoria")
    public RegistroPromocion recalcularTrasSupletoria(@RequestParam Long alumnoId, @RequestParam Long anioLectivoId) {
        return promocionService.recalcularTrasSupletoria(alumnoId, anioLectivoId);
    }

    @GetMapping
    public RegistroPromocion obtener(@RequestParam Long alumnoId, @RequestParam Long anioLectivoId) {
        return promocionService.obtenerPorAlumnoYAnio(alumnoId, anioLectivoId);
    }

    @PostMapping("/{id}/excepcional-media")
    public RegistroPromocion excepcionalMedia(@PathVariable Long id,
                                               @RequestParam Long refuerzoId,
                                               @RequestParam boolean acuerdoFirmado,
                                               @RequestParam String informeJunta) {
        return promocionService.registrarExcepcionalMedia(id, refuerzoId, acuerdoFirmado, informeJunta);
    }

    @PostMapping("/repitencia-excepcional")
    public RegistroPromocion repitenciaExcepcional(@RequestParam Long alumnoId,
                                                     @RequestParam Long anioLectivoId,
                                                     @RequestParam Long evaluacionPsicopedagogicaId,
                                                     @RequestParam boolean solicitudRepresentante,
                                                     @RequestParam String informeJunta) {
        return promocionService.registrarRepitenciaExcepcional(
                alumnoId, anioLectivoId, evaluacionPsicopedagogicaId, solicitudRepresentante, informeJunta);
    }
}
