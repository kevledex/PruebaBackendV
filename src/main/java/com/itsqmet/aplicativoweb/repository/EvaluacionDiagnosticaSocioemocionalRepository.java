package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.model.EvaluacionDiagnosticaSocioemocional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluacionDiagnosticaSocioemocionalRepository extends JpaRepository<EvaluacionDiagnosticaSocioemocional, Long> {
    List<EvaluacionDiagnosticaSocioemocional> findByAlumnoId(Long alumnoId);
}
