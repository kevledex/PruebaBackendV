package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.model.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DocenteRepository extends JpaRepository <Docente,Long>{
    Optional<Docente> findByUsuario_Id(Long usuarioId);
    long countByRolId(Long rolId);
}
