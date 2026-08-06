package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.model.AnioLectivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnioLectivoRepository extends JpaRepository<AnioLectivo, Long> {
    Optional<AnioLectivo> findByActivoTrue();
    List<AnioLectivo> findAllByActivoTrue();
}
