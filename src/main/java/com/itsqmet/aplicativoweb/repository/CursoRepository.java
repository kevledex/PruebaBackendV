package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CursoRepository extends JpaRepository<Curso, Long> {
    boolean existsByGradoAndParaleloAndAnioLectivoId(String grado, String paralelo, Long anioLectivoId);
    List<Curso> findByAnioLectivoId(Long anioLectivoId);
}
