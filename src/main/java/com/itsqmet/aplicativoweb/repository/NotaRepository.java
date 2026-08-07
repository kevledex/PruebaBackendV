package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.model.Nota;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NotaRepository extends JpaRepository<Nota, Long> {
    Optional<Nota> findByAlumnoIdAndActividadId(Long alumnoId, Long actividadId);

    List<Nota> findByAlumnoIdAndActividad_MateriaCursoIdAndActividad_PeriodoAcademicoId(
            Long alumnoId, Long materiaCursoId, Long periodoAcademicoId);

    List<Nota> findByActividad_MateriaCursoIdAndActividad_PeriodoAcademicoId(
            Long materiaCursoId, Long periodoAcademicoId);

    List<Nota> findByActividad_PeriodoAcademicoId(Long periodoAcademicoId);
}
