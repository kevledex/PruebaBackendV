package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.exception.RecursoNoEncontradoException;
import com.itsqmet.aplicativoweb.model.Usuario;
import com.itsqmet.aplicativoweb.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CORREGIDO: UsuarioController invoca usuarioService.listar(), .crear(),
 * .actualizar(), .eliminar() -- esta clase solo tenía obtenerTodos(),
 * ObtenerPorId() (con mayúscula inicial incorrecta), guardarUsuario() y
 * eliminarUsuario(). Se renombran/redefinen para que coincidan con el
 * controlador y se conserva la lógica de encriptado de contraseña.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listar(String rol) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        if (rol == null || rol.isBlank()) {
            return usuarios;
        }
        return usuarios.stream()
                .filter(usuario -> usuario.getRol() != null
                        && rol.equalsIgnoreCase(usuario.getRol().getNombre()))
                .toList();
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }

    public Usuario crear(Usuario usuario) {
        codificarContraseniaSiPresente(usuario);
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizar(Long id, Usuario datos) {
        Usuario actual = obtenerPorId(id);
        actual.setUsuario(datos.getUsuario());
        actual.setEstado(datos.getEstado());
        actual.setRol(datos.getRol());
        if (datos.getContrasenia() != null && !datos.getContrasenia().isBlank()) {
            actual.setContrasenia(passwordEncoder.encode(datos.getContrasenia()));
        }
        return usuarioRepository.save(actual);
    }

    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Usuario", id);
        }
        usuarioRepository.deleteById(id);
    }

    private void codificarContraseniaSiPresente(Usuario usuario) {
        if (usuario.getContrasenia() != null && !usuario.getContrasenia().isBlank()) {
            usuario.setContrasenia(passwordEncoder.encode(usuario.getContrasenia()));
        }
    }
}
