package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.model.EvaluacionComportamental;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvaluacionComportamentalRepository extends JpaRepository<EvaluacionComportamental, Long> {
    List<EvaluacionComportamental> findByAlumnoId(Long alumnoId);
    Optional<EvaluacionComportamental> findByAlumnoIdAndPeriodoAcademicoId(Long alumnoId, Long periodoAcademicoId);
}
