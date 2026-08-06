package com.itsqmet.aplicativoweb.service;
import com.itsqmet.aplicativoweb.dto.AuthDtos.LoginRequest;
import com.itsqmet.aplicativoweb.dto.AuthDtos.LoginResponse;
import com.itsqmet.aplicativoweb.dto.AuthDtos.SessionResponse;
import com.itsqmet.aplicativoweb.model.Usuario;
import com.itsqmet.aplicativoweb.repository.UsuarioRepository;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * CORREGIDO:
 *  1) El import apuntaba a "dto.Auth.Dtos.*" (paquete "Auth" + clase "Dtos"),
 *     inconsistente con la clase real "dto.AuthDtos" que usa AuthController.
 *     Se unifica a "dto.AuthDtos.*".
 *  2) "usuarios.findByUsuarioIgnorecase(...)" ahora es
 *     "findByUsuarioIgnoreCase(...)" y ya devuelve Optional<Usuario>
 *     directamente compatible con ".orElse(null)".
 *  3) "usuario.getPassword()" no existe: el campo de la entidad Usuario se
 *     llama "contrasenia" (Lombok genera "getContrasenia()"), no "password".
 *     Se corrige la llamada.
 */
@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    public ResultadoLogin autenticar(LoginRequest request) {
        Usuario usuario = usuarios.findByUsuarioIgnoreCase(request.usuario()).orElse(null);
        if (usuario == null
                || !"Activo".equals(usuario.getEstado())
                || !encoder.matches(request.password(), usuario.getContrasenia())) {
            return null;
        }

        List<GrantedAuthority> autoridades = new ArrayList<>();
        autoridades.add(new SimpleGrantedAuthority(
                "ROLE_" + usuario.getRol().getNombre().toUpperCase()));
        usuario.getRol().getPermisos().forEach(permiso ->
                autoridades.add(new SimpleGrantedAuthority(autoridad(permiso))));

        Authentication autenticacion = new UsernamePasswordAuthenticationToken(
                usuario.getUsuario(), null, autoridades);
        LoginResponse respuesta = new LoginResponse(
                usuario.getId(),
                usuario.getUsuario(),
                usuario.getRol().getId(),
                usuario.getRol().getNombre(),
                usuario.getRol().getPermisos());
        return new ResultadoLogin(autenticacion, respuesta);
    }

    public SessionResponse obtenerSesion(Authentication authentication) {
        Usuario usuario = usuarios.findByUsuarioIgnoreCase(authentication.getName())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED,
                        "Sesión no válida"));
        return new SessionResponse(
                usuario.getId(),
                usuario.getUsuario(),
                usuario.getRol().getId(),
                usuario.getRol().getNombre(),
                usuario.getRol().getPermisos());
    }

    private String autoridad(String permiso) {
        return "PERM_" + Normalizer.normalize(permiso, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase()
                .replace(' ', '_');
    }

    public record ResultadoLogin(Authentication autenticacion, LoginResponse respuesta) {}
}
