package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.model.RefuerzoPedagogico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefuerzoPedagogicoRepository extends JpaRepository<RefuerzoPedagogico, Long> {
    List<RefuerzoPedagogico> findByAlumnoId(Long alumnoId);
}
