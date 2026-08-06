package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.SolicitudRevision;
import com.itsqmet.aplicativoweb.service.RevisionCalificacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/solicitudes-revision")
public class SolicitudRevisionController {

    private final RevisionCalificacionService revisionCalificacionService;

    public SolicitudRevisionController(RevisionCalificacionService revisionCalificacionService) {
        this.revisionCalificacionService = revisionCalificacionService;
    }

    @PostMapping
    public ResponseEntity<SolicitudRevision> solicitar(@RequestParam Long notaId,
                                                         @RequestParam Long representanteId,
                                                         @RequestParam String motivo) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(revisionCalificacionService.solicitar(notaId, representanteId, motivo));
    }

    @GetMapping("/{id}")
    public SolicitudRevision obtenerPorId(@PathVariable Long id) {
        return revisionCalificacionService.obtenerPorId(id);
    }

    @PatchMapping("/{id}/comision")
    public SolicitudRevision asignarComision(@PathVariable Long id, @RequestBody List<Long> docenteIds) {
        return revisionCalificacionService.asignarComision(id, docenteIds);
    }

    @PatchMapping("/{id}/resolver")
    public SolicitudRevision resolver(@PathVariable Long id,
                                       @RequestParam boolean rectificar,
                                       @RequestParam(required = false) BigDecimal nuevaCalificacion,
                                       @RequestParam String informe) {
        return revisionCalificacionService.resolver(id, rectificar, nuevaCalificacion, informe);
    }

    @PatchMapping("/{id}/nueva-evaluacion")
    public SolicitudRevision marcarNuevaEvaluacionRequerida(@PathVariable Long id, @RequestParam String informe) {
        return revisionCalificacionService.marcarNuevaEvaluacionRequerida(id, informe);
    }

    @PostMapping("/{id}/apelar")
    public SolicitudRevision apelar(@PathVariable Long id) {
        return revisionCalificacionService.apelar(id);
    }

    @PatchMapping("/{id}/resolver-apelacion")
    public SolicitudRevision resolverApelacion(@PathVariable Long id,
                                                @RequestParam List<Long> comisionApelacionIds,
                                                @RequestParam String resolucion) {
        return revisionCalificacionService.resolverApelacion(id, comisionApelacionIds, resolucion);
    }
}
