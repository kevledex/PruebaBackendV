package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.model.EvaluacionPsicopedagogica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluacionPsicopedagogicaRepository extends JpaRepository<EvaluacionPsicopedagogica, Long> {
    List<EvaluacionPsicopedagogica> findByAlumnoId(Long alumnoId);
}
