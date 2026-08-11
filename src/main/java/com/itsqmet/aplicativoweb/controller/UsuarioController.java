package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.Usuario;
import com.itsqmet.aplicativoweb.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService){
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<Usuario> listar(@RequestParam(required = false) String rol){
        return usuarioService.listar(rol);
    }

    @PostMapping
    public Usuario crear(@RequestBody Usuario datos){
        return usuarioService.crear(datos);
    }

    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Long id, @RequestBody Usuario datos){
        return usuarioService.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
