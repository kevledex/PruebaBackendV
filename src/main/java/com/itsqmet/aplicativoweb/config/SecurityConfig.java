package com.itsqmet.aplicativoweb.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * AJUSTADO: se agregan las reglas de acceso para todos los endpoints nuevos
 * derivados del análisis MINEDUC (estructura académica, alertas, refuerzos,
 * mejoras, supletorias, revisión/apelación, socioemocional, NEE, promoción
 * y boletas). El resto de rutas no listadas explícitamente sigue cubierto
 * por el ".anyRequest().authenticated()" original, así que ningún endpoint
 * nuevo queda abierto sin autenticación.
 *
 * NOTA: "/api/mis-representados/**" ya estaba reservado para
 * ROLE_REPRESENTANTE en el archivo original, pero no existe (todavía) un
 * controlador para esa ruta ni un vínculo Usuario<->Representante en el
 * modelo de datos -- se deja la regla tal cual, pendiente de esa decisión
 * de diseño (ver nota en el listado final de archivos).
 */
@Configuration
@EnableWebSecurity //Enciende la seguridad web en toda la aplicación
public class SecurityConfig {

    // Herramienta para encriptar las contraseñas de los usuarios
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Administrador principal que procesa el inicio de sesión
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORREGIDO: ".cors(cors -> {})" activaba el soporte CORS de Spring
     * Security pero sin registrar ningún CorsConfigurationSource, por lo que
     * nunca se agregaban las cabeceras Access-Control-*. El frontend (Vite,
     * http://localhost:5173) llama al backend con "credentials: include"
     * (cookies de sesión), y un fetch cross-origin con credenciales sin esas
     * cabeceras es bloqueado por el navegador antes de llegar a Spring -> el
     * fetch() del frontend fallaba a nivel de red con el mensaje genérico
     * "No se pudo conectar con el servidor.".
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracion = new CorsConfiguration();
        // Cubre el puerto por defecto de Vite (5173) y cualquier otro puerto
        // local que Vite elija si el 5173 está ocupado.
        configuracion.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("*"));
        configuracion.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", configuracion);
        return fuente;
    }

    //Filtro principal para definir las reglas de acceso a las rutas
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Activa la configuración para conectar al frontend
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Desactiva la protección CSRF para poder probar POST, PUT y DELETE sin bloqueos
                .csrf(AbstractHttpConfigurer::disable)

                // Se define quien puede entrar a cada ruta
                .authorizeHttpRequests(auth -> auth

                        // Deja pasar siempre el preflight CORS (OPTIONS) antes de cualquier chequeo de rol
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/api/auth/**").permitAll()

                        // Administrador puede gestionar cursos, docentes y representantes
                        .requestMatchers("/api/cursos/**").hasRole("ADMIN")
                        .requestMatchers("/api/docentes/**").hasRole("ADMIN")
                        .requestMatchers("/api/representantes/**").hasRole("ADMIN")

                        // Administradores y docentes pueden tomar asistencias y ver alumnos
                        .requestMatchers("/api/alumnos/**").hasAnyRole("ADMIN", "DOCENTE")
                        .requestMatchers("/api/asistencias/**").hasAnyRole("ADMIN", "DOCENTE")

                        // Los representantes solo ven información de sus estudiantes
                        .requestMatchers("/api/mis-representados/**").hasRole("REPRESENTANTE")

                        // --- Estructura académica (Art. 4 del Acuerdo) ---
                        .requestMatchers("/api/anios-lectivos/**").hasRole("ADMIN")
                        .requestMatchers("/api/periodos/**").hasAnyRole("ADMIN", "DOCENTE")
                        .requestMatchers("/api/materias-curso/**").hasAnyRole("ADMIN", "DOCENTE")
                        .requestMatchers("/api/materias/**").hasAnyRole("ADMIN", "DOCENTE")

                        // --- Evaluación educativa (Art. 3, 5, 6, 7 del Acuerdo) ---
                        .requestMatchers("/api/actividades/**").hasAnyRole("ADMIN", "DOCENTE")
                        .requestMatchers("/api/notas/**").hasAnyRole("ADMIN", "DOCENTE")
                        .requestMatchers("/api/evaluaciones-destreza/**").hasAnyRole("ADMIN", "DOCENTE")

                        // --- Alerta temprana y refuerzo pedagógico (Art. 9, 3.j) ---
                        .requestMatchers("/api/alertas/**").hasAnyRole("ADMIN", "DOCENTE")
                        .requestMatchers("/api/refuerzos/**").hasAnyRole("ADMIN", "DOCENTE")

                        // --- Mejora de calificaciones y supletoria (Art. 10-12, 21-22) ---
                        .requestMatchers("/api/alumnos/*/mejoras/**").hasAnyRole("ADMIN", "DOCENTE")
                        .requestMatchers("/api/alumnos/*/supletorias/**").hasAnyRole("ADMIN", "DOCENTE")

                        // --- Revisión y apelación de calificaciones (Art. 40 RGLOEI) ---
                        .requestMatchers("/api/solicitudes-revision/**").hasAnyRole("ADMIN", "DOCENTE", "REPRESENTANTE")

                        // --- Evaluación socioemocional (Cap. IX) y NEE (Cap. X) ---
                        .requestMatchers("/api/habilidades-socioemocionales/**").hasAnyRole("ADMIN", "DOCENTE")
                        .requestMatchers("/api/alumnos/*/evaluacion-diagnostica-socioemocional/**").hasAnyRole("ADMIN", "DOCENTE")
                        .requestMatchers("/api/alumnos/*/evaluacion-comportamental/**").hasAnyRole("ADMIN", "DOCENTE")
                        .requestMatchers("/api/alumnos/*/evaluaciones-psicopedagogicas/**").hasAnyRole("ADMIN", "DOCENTE")

                        // --- Promoción y repitencia (Cap. VI y VIII) ---
                        .requestMatchers("/api/promociones/**").hasRole("ADMIN")

                        // --- Informes de aprendizaje / boletas (Art. 4.e, 37) ---
                        .requestMatchers("/api/alumnos/*/informes/**").hasAnyRole("ADMIN", "DOCENTE", "REPRESENTANTE")
                        .requestMatchers("/api/cursos/*/informes/**").hasAnyRole("ADMIN", "DOCENTE")

                        // --- Dashboard de reportes ---
                        .requestMatchers("/api/reportes/**").hasRole("ADMIN")

                        // Cualquier otra ruta no definida requiere iniciar sesión
                        .anyRequest().authenticated()
                )

                // Solo una sesión activa por usuario a la vez
                .sessionManagement(session -> session
                        .maximumSessions(1)
                )

                // Mensajes de error personalizados cuando falla el acceso
                .exceptionHandling(ex -> ex
                        // Error 401: Intenta entrar a una ruta protegida sin haber iniciado sesión
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(401);
                            response.getWriter().write(
                                    "{\"error\": \"No autenticado. Debes hacer login primero.\"}"
                            );
                        })

                        // Error: 403: Ya iniciado sesión, pero no tiene el rol necesario para esa ruta
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(403);
                            response.getWriter().write(
                                    "{\"error\": \"Acceso denegado. No tienes permisos para esta acción.\"}"
                            );
                        })
                );

        return http.build();
    }

}
