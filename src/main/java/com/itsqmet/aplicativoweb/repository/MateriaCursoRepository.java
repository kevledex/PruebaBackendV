package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.model.MateriaCurso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MateriaCursoRepository extends JpaRepository<MateriaCurso, Long> {
    List<MateriaCurso> findByCursoId(Long cursoId);
    List<MateriaCurso> findByDocenteId(Long docenteId);
    Optional<MateriaCurso> findByMateriaIdAndCursoId(Long materiaId, Long cursoId);
    boolean existsByMateriaIdAndCursoId(Long materiaId, Long cursoId);
}
