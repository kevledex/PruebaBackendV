package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.model.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {
    List<Asistencia> findByFechaAndMateriaId(LocalDate fecha, Long materiaId);
    Optional<Asistencia> findByAlumnoIdAndFechaAndMateriaId(Long alumnoId, LocalDate fecha, Long materiaId);
}