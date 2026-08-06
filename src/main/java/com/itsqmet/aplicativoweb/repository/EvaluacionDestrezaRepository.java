package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.model.EvaluacionDestreza;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluacionDestrezaRepository extends JpaRepository<EvaluacionDestreza, Long> {
    List<EvaluacionDestreza> findByAlumnoIdAndPeriodoAcademicoId(Long alumnoId, Long periodoAcademicoId);
    List<EvaluacionDestreza> findByAlumnoId(Long alumnoId);
}
