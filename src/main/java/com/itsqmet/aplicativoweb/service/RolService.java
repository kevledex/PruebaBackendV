package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.exception.OperacionNoPermitidaException;
import com.itsqmet.aplicativoweb.exception.RecursoNoEncontradoException;
import com.itsqmet.aplicativoweb.model.Rol;
import com.itsqmet.aplicativoweb.repository.RolRepository;
import com.itsqmet.aplicativoweb.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CORREGIDO: RolController (que extiende BaseCrudController y sobrescribe
 * lista()/crear()/actualizar()/eliminar()) invocaba rolService.listar(),
 * .crear(), .actualizar(), .eliminar() -- ninguno de esos métodos existía
 * en esta clase (tenía obtenerTodos/obtenerPorId/guardadRol/eliminarRol).
 * Se renombran/redefinen para que coincidan, y de paso se aprovecha el
 * campo transitorio Rol.usuarios (antes sin uso) y el método
 * UsuarioRepository.countByRolId (definido pero nunca invocado) para
 * reportar cuántos usuarios tiene cada rol, y se protege a los roles
 * marcados como "protegido" (p.ej. ADMIN) de ser eliminados o de quedar
 * sin usuarios asignados.
 */
@Service
public class RolService {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;

    public RolService(RolRepository rolRepository, UsuarioRepository usuarioRepository) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Rol> listar() {
        List<Rol> roles = rolRepository.findAll();
        roles.forEach(rol -> rol.setUsuarios(usuarioRepository.countByRolId(rol.getId())));
        return roles;
    }

    public Rol obtenerPorId(Long id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol", id));
        rol.setUsuarios(usuarioRepository.countByRolId(id));
        return rol;
    }

    public Rol crear(Rol rol) {
        rol.setProtegido(false);
        return rolRepository.save(rol);
    }

    public Rol actualizar(Long id, Rol datos) {
        Rol actual = rolRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol", id));
        actual.setNombre(datos.getNombre());
        actual.setEstado(datos.getEstado());
        actual.setPermisos(datos.getPermisos());
        // "protegido" no se modifica vía API para evitar desproteger roles críticos (ADMIN, etc.)
        return rolRepository.save(actual);
    }

    public void eliminar(Long id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol", id));
        if (rol.isProtegido()) {
            throw new OperacionNoPermitidaException("El rol '" + rol.getNombre() + "' está protegido y no puede eliminarse");
        }
        if (usuarioRepository.countByRolId(id) > 0) {
            throw new OperacionNoPermitidaException("No se puede eliminar el rol '" + rol.getNombre() + "' porque tiene usuarios asignados");
        }
        rolRepository.deleteById(id);
    }
}
