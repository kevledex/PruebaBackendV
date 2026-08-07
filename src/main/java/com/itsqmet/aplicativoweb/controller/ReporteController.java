package com.itsqmet.aplicativoweb.controller;

import com.itsqmet.aplicativoweb.dto.BoletaDtos;
import com.itsqmet.aplicativoweb.exception.RecursoNoEncontradoException;
import com.itsqmet.aplicativoweb.model.Alumno;
import com.itsqmet.aplicativoweb.model.Docente;
import com.itsqmet.aplicativoweb.model.PeriodoAcademico;
import com.itsqmet.aplicativoweb.model.Usuario;
import com.itsqmet.aplicativoweb.repository.AlumnoRepository;
import com.itsqmet.aplicativoweb.repository.DocenteRepository;
import com.itsqmet.aplicativoweb.repository.MateriaCursoRepository;
import com.itsqmet.aplicativoweb.repository.PeriodoAcademicoRepository;
import com.itsqmet.aplicativoweb.repository.UsuarioRepository;
import com.itsqmet.aplicativoweb.service.BoletaService;
import com.itsqmet.aplicativoweb.service.ReportePdfService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Genera el PDF de calificaciones (boleta) de un alumno. Docente solo puede
 * generarlo para alumnos de un curso donde tenga una MateriaCurso asignada;
 * Admin puede generarlo para cualquier alumno. La regla de rol (solo
 * ADMIN/DOCENTE) ya la impone SecurityConfig; aquí solo falta acotar al
 * docente a "su" curso, que SecurityConfig no puede expresar.
 */
@RestController
@RequestMapping("/api/alumnos/{alumnoId}/reportes")
public class ReporteController {

    private final BoletaService boletaService;
    private final ReportePdfService reportePdfService;
    private final AlumnoRepository alumnoRepository;
    private final UsuarioRepository usuarioRepository;
    private final DocenteRepository docenteRepository;
    private final MateriaCursoRepository materiaCursoRepository;
    private final PeriodoAcademicoRepository periodoAcademicoRepository;

    public ReporteController(BoletaService boletaService, ReportePdfService reportePdfService,
                              AlumnoRepository alumnoRepository, UsuarioRepository usuarioRepository,
                              DocenteRepository docenteRepository, MateriaCursoRepository materiaCursoRepository,
                              PeriodoAcademicoRepository periodoAcademicoRepository) {
        this.boletaService = boletaService;
        this.reportePdfService = reportePdfService;
        this.alumnoRepository = alumnoRepository;
        this.usuarioRepository = usuarioRepository;
        this.docenteRepository = docenteRepository;
        this.materiaCursoRepository = materiaCursoRepository;
        this.periodoAcademicoRepository = periodoAcademicoRepository;
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long alumnoId,
                                       @RequestParam(required = false) Long periodo,
                                       @RequestParam(required = false) Long anioLectivo,
                                       Authentication authentication) {
        Alumno alumno = alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno", alumnoId));

        validarAccesoDocente(authentication, alumno);

        byte[] pdf;
        if (periodo != null) {
            PeriodoAcademico periodoAcademico = periodoAcademicoRepository.findById(periodo)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Periodo académico", periodo));
            BoletaDtos.InformeProgresoDto informe = boletaService.generarInformeProgreso(alumnoId, periodo);
            pdf = reportePdfService.generarPdfProgreso(informe, alumno, periodoAcademico);
        } else if (anioLectivo != null) {
            BoletaDtos.InformeFinalAnualDto informe = boletaService.generarInformeFinalAnual(alumnoId, anioLectivo);
            pdf = reportePdfService.generarPdfAnual(informe, alumno);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe indicar 'periodo' (un trimestre) o 'anioLectivo' (reporte general)");
        }

        String archivo = "reporte_" + alumno.getApellidos().replaceAll("\\s+", "_") + "_" + alumnoId + ".pdf";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.inline().filename(archivo).build());
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private void validarAccesoDocente(Authentication authentication, Alumno alumno) {
        Usuario usuario = usuarioRepository.findByUsuarioIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión no válida"));

        if (!"Docente".equalsIgnoreCase(usuario.getRol().getNombre())) {
            return; // Admin (u otro rol permitido por SecurityConfig): sin restricción adicional
        }

        Docente docente = docenteRepository.findByUsuario_Id(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Este usuario no está vinculado a ningún docente"));

        Set<Long> cursosDelDocente = materiaCursoRepository.findByDocenteId(docente.getId()).stream()
                .map(materiaCurso -> materiaCurso.getCurso().getId())
                .collect(Collectors.toSet());

        if (!cursosDelDocente.contains(alumno.getCurso().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo puedes generar reportes de estudiantes de tu curso asignado");
        }
    }
}
