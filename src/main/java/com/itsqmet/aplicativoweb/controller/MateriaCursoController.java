package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.MateriaCurso;
import com.itsqmet.aplicativoweb.service.MateriaCursoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/materias-curso")
public class MateriaCursoController {

    private final MateriaCursoService materiaCursoService;

    public MateriaCursoController(MateriaCursoService materiaCursoService) {
        this.materiaCursoService = materiaCursoService;
    }

    @GetMapping("/{id}")
    public MateriaCurso obtenerPorId(@PathVariable Long id) {
        return materiaCursoService.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    public MateriaCurso actualizar(@PathVariable Long id, @Valid @RequestBody MateriaCurso materiaCurso) {
        return materiaCursoService.actualizar(id, materiaCurso);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        materiaCursoService.eliminar(id);
    }

    @GetMapping("/{id}/aportes-requeridos")
    public Map<String, Integer> aportesRequeridos(@PathVariable Long id) {
        return Map.of("aportesRequeridos", materiaCursoService.aportesRequeridos(id));
    }
}
