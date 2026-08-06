package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.model.PromedioMateriaPeriodo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PromedioMateriaPeriodoRepository extends JpaRepository<PromedioMateriaPeriodo, Long> {
    Optional<PromedioMateriaPeriodo> findByAlumnoIdAndMateriaCursoIdAndPeriodoAcademicoId(
            Long alumnoId, Long materiaCursoId, Long periodoAcademicoId);

    List<PromedioMateriaPeriodo> findByAlumnoIdAndPeriodoAcademicoId(Long alumnoId, Long periodoAcademicoId);

    List<PromedioMateriaPeriodo> findByAlumnoIdAndMateriaCursoId(Long alumnoId, Long materiaCursoId);
}
