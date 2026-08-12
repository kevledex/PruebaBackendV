package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.model.PeriodoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PeriodoAcademicoRepository extends JpaRepository<PeriodoAcademico, Long> {
    List<PeriodoAcademico> findByCursoIdOrderByNumeroAsc(Long cursoId);
    boolean existsByCursoId(Long cursoId);
    boolean existsByIdAndCerradoTrue(Long id);
}
