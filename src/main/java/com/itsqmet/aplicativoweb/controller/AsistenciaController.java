package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.dto.AsistenciaDtos.RegistroLote;
import com.itsqmet.aplicativoweb.model.Asistencia;
import com.itsqmet.aplicativoweb.service.AsistenciaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * NUEVO: SecurityConfig ya reservaba la ruta "/api/asistencias/**" para
 * ADMIN/DOCENTE, pero no existía ningún controlador -- la entidad, el
 * servicio y el repositorio (con su método de upsert
 * "findByAlumnoIdAndFechaAndMateriaId") estaban listos pero sin exponerse
 * por HTTP, por lo que el frontend nunca pudo registrar asistencia.
 */
@RestController
@RequestMapping("/api/asistencias")
@CrossOrigin(origins = "*")
public class AsistenciaController {

    @Autowired
    private AsistenciaService asistenciaService;

    @GetMapping
    public ResponseEntity<List<Asistencia>> listar(
            @RequestParam(required = false) LocalDate fecha,
            @RequestParam(required = false) Long materiaId) {
        List<Asistencia> asistencias = (fecha != null && materiaId != null)
                ? asistenciaService.listarPorFechaYMateria(fecha, materiaId)
                : asistenciaService.obtenerTodas();
        return ResponseEntity.ok(asistencias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Asistencia> obtenerPorId(@PathVariable Long id) {
        return asistenciaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Asistencia> crear(@Valid @RequestBody Asistencia asistencia) {
        Asistencia nueva = asistenciaService.guardarAsistencia(asistencia);
        return new ResponseEntity<>(nueva, HttpStatus.CREATED);
    }

    @PostMapping("/lote")
    public ResponseEntity<List<Asistencia>> registrarLote(@Valid @RequestBody RegistroLote registro) {
        List<Asistencia> guardadas = asistenciaService.guardarLote(
                registro.fecha(), registro.materiaId(), registro.estudiantes());
        return ResponseEntity.ok(guardadas);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Asistencia> actualizar(@PathVariable Long id, @Valid @RequestBody Asistencia asistencia) {
        return asistenciaService.obtenerPorId(id).map(existente -> {
            asistencia.setId(id);
            return ResponseEntity.ok(asistenciaService.guardarAsistencia(asistencia));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (asistenciaService.obtenerPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        asistenciaService.eliminarAsistencia(id);
        return ResponseEntity.noContent().build();
    }
}
