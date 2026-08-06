package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.model.RegistroPromocion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegistroPromocionRepository extends JpaRepository<RegistroPromocion, Long> {
    List<RegistroPromocion> findByAlumnoId(Long alumnoId);
    Optional<RegistroPromocion> findByAlumnoIdAndAnioLectivoId(Long alumnoId, Long anioLectivoId);
    boolean existsByAlumnoIdAndYaAplicoRepitenciaExcepcionalAntesTrue(Long alumnoId);
}
