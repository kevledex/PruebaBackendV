package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.AnioLectivo;
import com.itsqmet.aplicativoweb.service.AnioLectivoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anios-lectivos")
public class AnioLectivoController {

    private final AnioLectivoService anioLectivoService;

    public AnioLectivoController(AnioLectivoService anioLectivoService) {
        this.anioLectivoService = anioLectivoService;
    }

    @GetMapping
    public List<AnioLectivo> listar() {
        return anioLectivoService.listar();
    }

    @GetMapping("/activo")
    public AnioLectivo obtenerActivo() {
        return anioLectivoService.obtenerActivo();
    }

    @GetMapping("/{id}")
    public AnioLectivo obtenerPorId(@PathVariable Long id) {
        return anioLectivoService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<AnioLectivo> crear(@Valid @RequestBody AnioLectivo anioLectivo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(anioLectivoService.crear(anioLectivo));
    }

    @PutMapping("/{id}")
    public AnioLectivo actualizar(@PathVariable Long id, @Valid @RequestBody AnioLectivo anioLectivo) {
        return anioLectivoService.actualizar(id, anioLectivo);
    }

    @PatchMapping("/{id}/activar")
    public AnioLectivo activar(@PathVariable Long id) {
        return anioLectivoService.activar(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        anioLectivoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
