package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.Representante;
import com.itsqmet.aplicativoweb.service.RepresentanteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * NUEVO: SecurityConfig ya reservaba "/api/representantes/**" para
 * ROLE_ADMIN, pero la entidad, el servicio y el repositorio no tenían
 * ningún controlador que los expusiera por HTTP (a diferencia de
 * "/api/mis-representados/**", que sigue pendiente a propósito -- ver nota
 * en SecurityConfig).
 */
@RestController
@RequestMapping("/api/representantes")
@CrossOrigin(origins = "*")
public class RepresentanteController {

    @Autowired
    private RepresentanteService representanteService;

    @GetMapping
    public ResponseEntity<List<Representante>> listar() {
        return ResponseEntity.ok(representanteService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Representante> obtenerPorId(@PathVariable Long id) {
        return representanteService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Representante> crear(@Valid @RequestBody Representante representante) {
        Representante nuevo = representanteService.guardarRepresentante(representante);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Representante> actualizar(@PathVariable Long id, @Valid @RequestBody Representante representante) {
        return representanteService.obtenerPorId(id).map(existente -> {
            representante.setId(id);
            return ResponseEntity.ok(representanteService.guardarRepresentante(representante));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (representanteService.obtenerPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        representanteService.eliminarRepresentante(id);
        return ResponseEntity.noContent().build();
    }
}
