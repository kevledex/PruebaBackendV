package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.model.RegistroPromocion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistroPromocionRepository extends JpaRepository<RegistroPromocion, Long> {
    Optional<RegistroPromocion> findByAlumnoIdAndAnioLectivoId(Long alumnoId, Long anioLectivoId);
}
