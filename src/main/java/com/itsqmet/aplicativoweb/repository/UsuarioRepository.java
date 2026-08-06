package com.itsqmet.aplicativoweb.repository;


import com.itsqmet.aplicativoweb.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * CORREGIDO:
 *  - "findByUsuarioIgnorecase" devolvía List<Usuario> pero AuthService lo
 *    usaba como Optional (".orElse(null)"), lo cual no compila sobre un
 *    List. Se cambia el tipo de retorno a Optional<Usuario>.
 *  - El nombre del método no respetaba la palabra clave "IgnoreCase" que
 *    Spring Data reconoce para derivar la consulta (estaba en minúscula:
 *    "Ignorecase"); se corrige a "findByUsuarioIgnoreCase".
 */
public interface UsuarioRepository extends JpaRepository<Usuario,Long> {

    Optional<Usuario> findByUsuarioIgnoreCase(String usuario);
    long countByRolId(Long rolId);
}
