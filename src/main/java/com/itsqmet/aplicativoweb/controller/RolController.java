package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.model.Rol;
import com.itsqmet.aplicativoweb.repository.RolRepository;
import com.itsqmet.aplicativoweb.service.RolService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CORREGIDO:
 *  1) Faltaba un ";" al final de "this.rolService = rolService" (error de
 *     sintaxis).
 *  2) Se sobrescribía un método "listar()" con @Override, pero
 *     BaseCrudController declara "lista()" (sin la "r"); al no existir tal
 *     método en la superclase, "@Override" provoca un error de compilación.
 *     Se corrige el nombre a "lista()".
 *  3) Los nombres ahora coinciden con los métodos reales de RolService
 *     (listar/crear/actualizar/eliminar).
 */
@RestController
@RequestMapping("/api/roles")
public class RolController extends BaseCrudController<Rol> {
    private final RolService rolService;

    public  RolController(RolRepository roles, RolService rolService){
        super(roles);
        this.rolService = rolService;
    }

    @Override
    public List<Rol> lista(){
        return  rolService.listar();
    }

    @Override
    public ResponseEntity<Rol> crear(@Valid @RequestBody Rol body){
        return ResponseEntity.status(HttpStatus.CREATED).body(rolService.crear(body));
    }

    @Override
    public Rol actualizar(@PathVariable Long id, @Valid @RequestBody Rol body){
        return  rolService.actualizar(id,body);
    }

    @Override
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        rolService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
