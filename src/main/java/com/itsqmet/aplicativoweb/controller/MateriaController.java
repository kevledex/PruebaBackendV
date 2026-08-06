package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.Materia;
import com.itsqmet.aplicativoweb.service.MateriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/materias")
@CrossOrigin(origins = "*")
public class MateriaController {

    @Autowired
    private MateriaService materiaService;
    //GET : Obtener todas las materias
    @GetMapping
    public ResponseEntity<List<Materia>> listarTodas() {
        List<Materia> materias = materiaService.obtenerTodo();
        return ResponseEntity.ok(materias);
    }
    //GET: Obtener materia por ID
    @GetMapping("/{id}")
    public ResponseEntity<Materia> obtenerPorId(@PathVariable Long id) {
        Materia materia =materiaService.buscarPorId(id);
        return ResponseEntity.ok(materia);
    }
    //POST:Crea una nueva materia
    @PostMapping
    public ResponseEntity<Materia> guardar(@RequestBody Materia materia) {
        Materia nuevaMateria = materiaService.crearMateria(materia);
        return new ResponseEntity<>(nuevaMateria, HttpStatus.CREATED);
    }
    //PUT: Actualiza una materia existente
    @PutMapping("/{id}")
    public ResponseEntity<Materia> actualizarMateria(@PathVariable Long id ,@RequestBody Materia materia){
        Materia materiaActualizada = materiaService.actualizar(id, materia);
        return ResponseEntity.ok(materiaActualizada);
    }
    //DELETE: Elimina una materia por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMateria(@PathVariable Long id) {
        materiaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}