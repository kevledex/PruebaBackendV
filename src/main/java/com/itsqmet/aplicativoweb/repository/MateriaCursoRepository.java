package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.model.MateriaCurso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MateriaCursoRepository extends JpaRepository<MateriaCurso, Long> {
    List<MateriaCurso> findByCursoId(Long cursoId);
    List<MateriaCurso> findByDocenteId(Long docenteId);
    boolean existsByMateriaIdAndCursoId(Long materiaId, Long cursoId);
}
