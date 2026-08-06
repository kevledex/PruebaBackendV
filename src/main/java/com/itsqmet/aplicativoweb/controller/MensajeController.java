package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.Mensaje;
import com.itsqmet.aplicativoweb.service.MensajeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mensajes")
public class MensajeController {

    @Autowired
    private MensajeService mensajeService;

    @GetMapping
    public List<Mensaje> listar() {
        return mensajeService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mensaje> buscarPorId(@PathVariable Long id) {
        return mensajeService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mensaje crear(@Valid @RequestBody Mensaje mensaje) {
        return mensajeService.guardarMensaje(mensaje);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (mensajeService.obtenerPorId(id).isPresent()) {
            mensajeService.eliminarMensaje(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}