package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.Curso;
import com.itsqmet.aplicativoweb.model.MateriaCurso;
import com.itsqmet.aplicativoweb.model.PeriodoAcademico;
import com.itsqmet.aplicativoweb.service.CursoService;
import com.itsqmet.aplicativoweb.service.MateriaCursoService;
import com.itsqmet.aplicativoweb.service.PeriodoAcademicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * NUEVO: SecurityConfig ya reservaba "/api/cursos/**" para ROLE_ADMIN, pero
 * no existía ningún controlador para esa ruta.
 */
@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoService cursoService;
    private final PeriodoAcademicoService periodoAcademicoService;
    private final MateriaCursoService materiaCursoService;

    public CursoController(CursoService cursoService,
                            PeriodoAcademicoService periodoAcademicoService,
                            MateriaCursoService materiaCursoService) {
        this.cursoService = cursoService;
        this.periodoAcademicoService = periodoAcademicoService;
        this.materiaCursoService = materiaCursoService;
    }

    @GetMapping
    public List<Curso> listar(@RequestParam(required = false) Long anioLectivoId) {
        return anioLectivoId != null
                ? cursoService.listarPorAnioLectivo(anioLectivoId)
                : cursoService.listar();
    }

    @GetMapping("/{id}")
    public Curso obtenerPorId(@PathVariable Long id) {
        return cursoService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<Curso> crear(@Valid @RequestBody Curso curso) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cursoService.crear(curso));
    }

    @PutMapping("/{id}")
    public Curso actualizar(@PathVariable Long id, @Valid @RequestBody Curso curso) {
        return cursoService.actualizar(id, curso);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cursoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/periodos")
    public List<PeriodoAcademico> periodos(@PathVariable Long id) {
        return periodoAcademicoService.listarPorCurso(id);
    }

    @PostMapping("/{id}/periodos/generar")
    public List<PeriodoAcademico> generarPeriodos(@PathVariable Long id) {
        return periodoAcademicoService.generarPeriodos(id);
    }

    @GetMapping("/{id}/materias")
    public List<MateriaCurso> materias(@PathVariable Long id) {
        return materiaCursoService.listarPorCurso(id);
    }

    @PostMapping("/{id}/materias")
    public ResponseEntity<MateriaCurso> agregarMateria(@PathVariable Long id, @RequestBody MateriaCurso materiaCurso) {
        // CORREGIDO: "@Valid" validaba "materiaCurso" (incluido su "curso"
        // @NotNull) ANTES de que esta línea lo asignara desde el path
        // variable, por lo que la petición siempre rechazaba con "curso: El
        // curso es obligatorio" aunque el cliente sí hubiera elegido un
        // curso real. Se asigna primero y se deja que la validación
        // automática de JPA/Hibernate al guardar (vía MateriaCursoService)
        // valide el objeto ya completo.
        materiaCurso.setCurso(cursoService.obtenerPorId(id));
        return ResponseEntity.status(HttpStatus.CREATED).body(materiaCursoService.crear(materiaCurso));
    }
}
