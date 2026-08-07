package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.model.EvaluacionComportamental;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EvaluacionComportamentalRepository extends JpaRepository<EvaluacionComportamental, Long> {
    Optional<EvaluacionComportamental> findByAlumnoIdAndPeriodoAcademicoId(Long alumnoId, Long periodoAcademicoId);
}
