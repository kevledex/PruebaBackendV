package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.Alumno;
import com.itsqmet.aplicativoweb.service.AlumnoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CORREGIDO: "actualizarAlumno" recibía "@PathVariable Long id" pero el
 * método estaba mapeado con "@PutMapping" sin plantilla de ruta ("/{id}"),
 * lo que provoca el error de Spring MVC "Missing URI template variable 'id'"
 * al arrancar la aplicación. Se agrega "/{id}" al mapping.
 */
@RestController
@RequestMapping("/api/alumnos")
public class AlumnoController {

    @Autowired
    private AlumnoService alumnoService;
    //GET: Obtener todos los alumnos
    @GetMapping
    public ResponseEntity<List<Alumno>> obtenerTodos() {
        List<Alumno> alumnos = alumnoService.obtenerTodo();
        return ResponseEntity.ok(alumnos);
    }
    //GET: Obtener alumno por ID
    @GetMapping("/{id}")
    public ResponseEntity<Alumno> obtenerPorId(@PathVariable Long id) {
        Alumno alumno = alumnoService.buscarPorId(id);
        return ResponseEntity.ok(alumno);
    }
      //POST: crear un nuevo alumno
    @PostMapping
    public ResponseEntity<Alumno> crearAlumno(@Valid @RequestBody Alumno alumno){
        Alumno nuevoAlumno = alumnoService.crearAlumno(alumno);
        return new ResponseEntity<>(nuevoAlumno, HttpStatus.CREATED);
    }
    //PUT: Actualizar Alumno
    @PutMapping("/{id}")
    public ResponseEntity<Alumno> actualizarAlumno(@PathVariable Long id, @Valid @RequestBody Alumno alumno){
        Alumno alumnoActualizado = alumnoService.actualizar(id, alumno);
        return ResponseEntity.ok(alumnoActualizado);
    }
    //DELETE: Eliminar alumno
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAlumno(@PathVariable Long id) {
        alumnoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
