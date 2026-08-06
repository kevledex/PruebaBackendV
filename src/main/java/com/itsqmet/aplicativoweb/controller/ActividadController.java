package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.Actividad;
import com.itsqmet.aplicativoweb.service.ActividadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CORREGIDO/AMPLIADO:
 *  - BUG: "eliminar" devolvía "notFound()" incluso cuando el borrado se
 *    completaba correctamente (debía devolver 204 No Content). Se corrige.
 *  - Se agregan los endpoints de evaluaciones anticipadas y atrasadas
 *    (Art. 16 y 17 del Acuerdo Ministerial).
 */
@RestController
@RequestMapping("/api/actividades")
public class ActividadController {

    private final ActividadService actividadService;

    public ActividadController(ActividadService actividadService) {
        this.actividadService = actividadService;
    }

    @GetMapping
    public List<Actividad> listar() {
        return actividadService.obtenerTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Actividad> obtenerPorId(@PathVariable Long id) {
        return actividadService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Actividad crear(@Valid @RequestBody Actividad actividad) {
        return actividadService.guardarActividad(actividad);
    }

    @PostMapping("/anticipadas")
    public Actividad crearAnticipada(@Valid @RequestBody Actividad actividad,
                                      @RequestParam(defaultValue = "false") boolean esEvaluacionFinal) {
        return actividadService.registrarAnticipada(actividad, esEvaluacionFinal);
    }

    @PostMapping("/atrasadas")
    public Actividad crearAtrasada(@Valid @RequestBody Actividad actividad,
                                    @RequestParam(defaultValue = "false") boolean esEvaluacionFinal) {
        return actividadService.registrarAtrasada(actividad, esEvaluacionFinal);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Actividad> actualizar(@PathVariable Long id, @Valid @RequestBody Actividad actividadDetalles) {
        return actividadService.obtenerPorId(id).map(actividad -> {
            actividad.setNombre(actividadDetalles.getNombre());
            actividad.setTipoEvaluacion(actividadDetalles.getTipoEvaluacion());
            actividad.setCaracter(actividadDetalles.getCaracter());
            actividad.setEsProyectoInterdisciplinar(actividadDetalles.isEsProyectoInterdisciplinar());
            actividad.setPeriodoAcademico(actividadDetalles.getPeriodoAcademico());
            actividad.setFecha(actividadDetalles.getFecha());
            actividad.setMateriaCurso(actividadDetalles.getMateriaCurso());
            Actividad actualizada = actividadService.guardarActividad(actividad);
            return ResponseEntity.ok(actualizada);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (actividadService.obtenerPorId(id).isPresent()) {
            actividadService.eliminarActividad(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
