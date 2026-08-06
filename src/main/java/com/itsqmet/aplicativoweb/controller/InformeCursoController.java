package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.dto.BoletaDtos;
import com.itsqmet.aplicativoweb.model.Alumno;
import com.itsqmet.aplicativoweb.repository.AlumnoRepository;
import com.itsqmet.aplicativoweb.service.BoletaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * NUEVO. Permite generar el informe de progreso de todo un curso en lote
 * (útil para impresión masiva de boletas al cierre de un periodo).
 */
@RestController
@RequestMapping("/api/cursos/{cursoId}/informes")
public class InformeCursoController {

    private final BoletaService boletaService;
    private final AlumnoRepository alumnoRepository;

    public InformeCursoController(BoletaService boletaService, AlumnoRepository alumnoRepository) {
        this.boletaService = boletaService;
        this.alumnoRepository = alumnoRepository;
    }

    @GetMapping("/progreso")
    public List<BoletaDtos.InformeProgresoDto> progresoCurso(@PathVariable Long cursoId, @RequestParam Long periodo) {
        return alumnoRepository.findByCursoId(cursoId).stream()
                .map(Alumno::getId)
                .map(alumnoId -> boletaService.generarInformeProgreso(alumnoId, periodo))
                .toList();
    }
}
