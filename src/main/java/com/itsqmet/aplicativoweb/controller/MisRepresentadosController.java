package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.exception.RecursoNoEncontradoException;
import com.itsqmet.aplicativoweb.model.Alumno;
import com.itsqmet.aplicativoweb.model.Representante;
import com.itsqmet.aplicativoweb.model.Usuario;
import com.itsqmet.aplicativoweb.repository.AlumnoRepository;
import com.itsqmet.aplicativoweb.repository.RepresentanteRepository;
import com.itsqmet.aplicativoweb.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementa la ruta "/api/mis-representados", ya reservada en SecurityConfig
 * para ROLE_REPRESENTANTE, ahora que Representante tiene un vínculo real con
 * Usuario. Devuelve únicamente los alumnos cuyo representanteRegistro
 * corresponde al usuario autenticado.
 */
@RestController
@RequestMapping("/api/mis-representados")
public class MisRepresentadosController {

    private final UsuarioRepository usuarios;
    private final RepresentanteRepository representantes;
    private final AlumnoRepository alumnos;

    public MisRepresentadosController(UsuarioRepository usuarios,
                                       RepresentanteRepository representantes,
                                       AlumnoRepository alumnos) {
        this.usuarios = usuarios;
        this.representantes = representantes;
        this.alumnos = alumnos;
    }

    @GetMapping
    public List<Alumno> misHijos(Authentication authentication) {
        Usuario usuario = usuarios.findByUsuarioIgnoreCase(authentication.getName())
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesión no válida"));
        Representante representante = representantes.findByUsuario_Id(usuario.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Este usuario no está vinculado a ningún representante"));
        return alumnos.findByRepresentanteRegistro_Id(representante.getId());
    }
}
