package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.Curso;
import com.itsqmet.aplicativoweb.model.Docente;
import com.itsqmet.aplicativoweb.model.Usuario;
import com.itsqmet.aplicativoweb.repository.DocenteRepository;
import com.itsqmet.aplicativoweb.repository.MateriaCursoRepository;
import com.itsqmet.aplicativoweb.repository.UsuarioRepository;
import com.itsqmet.aplicativoweb.service.CursoService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Cursos visibles para el módulo "Generar reporte": Admin ve todos, Docente
 * solo los suyos (vía MateriaCurso). Mismo espíritu que
 * MisRepresentadosController, pero para el par ADMIN/DOCENTE en vez de
 * REPRESENTANTE.
 */
@RestController
@RequestMapping("/api/mis-cursos")
public class MisCursosController {

    private final UsuarioRepository usuarios;
    private final DocenteRepository docentes;
    private final MateriaCursoRepository materiasCurso;
    private final CursoService cursoService;

    public MisCursosController(UsuarioRepository usuarios, DocenteRepository docentes,
                                MateriaCursoRepository materiasCurso, CursoService cursoService) {
        this.usuarios = usuarios;
        this.docentes = docentes;
        this.materiasCurso = materiasCurso;
        this.cursoService = cursoService;
    }

    @GetMapping
    public List<Curso> misCursos(Authentication authentication) {
        Usuario usuario = usuarios.findByUsuarioIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión no válida"));

        if ("Admin".equalsIgnoreCase(usuario.getRol().getNombre())) {
            return cursoService.listar();
        }

        Docente docente = docentes.findByUsuario_Id(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Este usuario no está vinculado a ningún docente"));

        return materiasCurso.findByDocenteId(docente.getId()).stream()
                .map(materiaCurso -> materiaCurso.getCurso())
                .distinct()
                .toList();
    }
}
