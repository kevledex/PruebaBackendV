package com.itsqmet.aplicativoweb.repository;

import com.itsqmet.aplicativoweb.model.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * AJUSTADO: se agrega "findByCursoId", necesario ahora que Alumno se
 * relaciona con Curso (en vez de los campos "grado"/"paralelo" de texto
 * libre) y para generar boletas/informes por curso completo.
 */
public interface AlumnoRepository extends JpaRepository < Alumno,Long>{
    List<Alumno> findByCursoId(Long cursoId);
    List<Alumno> findByRepresentanteRegistro_Id(Long representanteId);
}
