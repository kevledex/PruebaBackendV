package com.itsqmet.aplicativoweb.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

/**
 * NUEVO: esta clase no existía en el proyecto, pero tanto AuthController
 * como AuthService la importaban ("dto.Auth.Dtos.*" y "dto.AuthDtos.*"
 * respectivamente), por lo que el módulo de autenticación no compilaba.
 * Se define un único contenedor "AuthDtos" con los tres records esperados.
 */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank(message = "El usuario es obligatorio") String usuario,
            @NotBlank(message = "La contraseña es obligatoria") String password
    ) {}

    public record LoginResponse(
            Long id,
            String usuario,
            Long rolId,
            String rolNombre,
            Set<String> permisos
    ) {}

    public record SessionResponse(
            Long id,
            String usuario,
            Long rolId,
            String rolNombre,
            Set<String> permisos
    ) {}
}
