package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.Nota;
import com.itsqmet.aplicativoweb.service.NotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notas")
public class NotaController {

    @Autowired
    private NotaService notaService;

    // GET: Obtener todas las notas
    @GetMapping
    public ResponseEntity<List<Nota>> obtenerTodas() {
        List<Nota> notas = notaService.obtenerTodo();
        return ResponseEntity.ok(notas);
    }

    // GET: Obtener nota por ID
    @GetMapping("/{id}")
    public ResponseEntity<Nota> obtenerPorId(@PathVariable Long id) {
        Nota nota = notaService.buscarPorId(id);
        return ResponseEntity.ok(nota);
    }

    // POST: crear una nueva nota
    @PostMapping
    public ResponseEntity<Nota> crearNota(@RequestBody Nota nota) {
        Nota nuevaNota = notaService.crearNota(nota);
        return new ResponseEntity<>(nuevaNota, HttpStatus.CREATED);
    }
    // PUT: Actualizar una nota existente
    @PutMapping("/{id}")
    public ResponseEntity<Nota> actualizarNota(@PathVariable Long id, @RequestBody Nota nota) {
        Nota notaActualizada = notaService.actualizar(id, nota);
        return ResponseEntity.ok(notaActualizada);
    }

    // DELEGTE: Eliminar una nota por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNota(@PathVariable Long id) {
        notaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}