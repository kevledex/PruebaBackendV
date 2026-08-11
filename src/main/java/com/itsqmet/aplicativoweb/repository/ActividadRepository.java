package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.model.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActividadRepository  extends JpaRepository<Actividad,Long> {
    List<Actividad> findByMateriaCursoIdAndPeriodoAcademicoId(Long materiaCursoId, Long periodoAcademicoId);
}
