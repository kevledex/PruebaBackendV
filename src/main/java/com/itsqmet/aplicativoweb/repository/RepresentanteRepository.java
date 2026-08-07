package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.model.Representante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepresentanteRepository extends JpaRepository<Representante,Long> {
    Optional<Representante> findByUsuario_Id(Long usuarioId);
}
