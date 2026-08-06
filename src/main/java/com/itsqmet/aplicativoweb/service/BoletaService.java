package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.dto.BoletaDtos;
import com.itsqmet.aplicativoweb.enums.CaracterEvaluacion;
import com.itsqmet.aplicativoweb.enums.NivelEducativo;
import com.itsqmet.aplicativoweb.enums.TipoPromocion;
import com.itsqmet.aplicativoweb.exception.RecursoNoEncontradoException;
import com.itsqmet.aplicativoweb.model.*;
import com.itsqmet.aplicativoweb.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * NUEVO. Es el reemplazo funcional del "ReporteService" que faltaba (el
 * ReporteController original importaba clases inexistentes). Construye los
 * dos tipos de informe exigidos por el Instructivo (Cap. 8.1, pág. 42):
 * informe de progreso (por periodo) e informe final anual, replicando la
 * estructura de las Tablas 24-36 (materias + promedio, fila de comportamiento,
 * destrezas para Inicial/Preparatoria, columna de supletorio, resultado de
 * promoción).
 */
@Service
public class BoletaService {

    private final AlumnoRepository alumnoRepository;
    private final MateriaCursoRepository materiaCursoRepository;
    private final PromedioMateriaPeriodoRepository promedioMateriaPeriodoRepository;
    private final EvaluacionDestrezaRepository evaluacionDestrezaRepository;
    private final EvaluacionComportamentalRepository evaluacionComportamentalRepository;
    private final PeriodoAcademicoRepository periodoAcademicoRepository;
    private final PromedioService promedioService;
    private final RegistroPromocionRepository registroPromocionRepository;
    private final NotaRepository notaRepository;

    public BoletaService(AlumnoRepository alumnoRepository,
                          MateriaCursoRepository materiaCursoRepository,
                          PromedioMateriaPeriodoRepository promedioMateriaPeriodoRepository,
                          EvaluacionDestrezaRepository evaluacionDestrezaRepository,
                          EvaluacionComportamentalRepository evaluacionComportamentalRepository,
                          PeriodoAcademicoRepository periodoAcademicoRepository,
                          PromedioService promedioService,
                          RegistroPromocionRepository registroPromocionRepository,
                          NotaRepository notaRepository) {
        this.alumnoRepository = alumnoRepository;
        this.materiaCursoRepository = materiaCursoRepository;
        this.promedioMateriaPeriodoRepository = promedioMateriaPeriodoRepository;
        this.evaluacionDestrezaRepository = evaluacionDestrezaRepository;
        this.evaluacionComportamentalRepository = evaluacionComportamentalRepository;
        this.periodoAcademicoRepository = periodoAcademicoRepository;
        this.promedioService = promedioService;
        this.registroPromocionRepository = registroPromocionRepository;
        this.notaRepository = notaRepository;
    }

    public BoletaDtos.InformeProgresoDto generarInformeProgreso(Long alumnoId, Long periodoAcademicoId) {
        Alumno alumno = alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno", alumnoId));
        NivelEducativo nivel = alumno.getCurso().getNivel();

        List<BoletaDtos.LineaMateriaDto> materias = new ArrayList<>();
        List<BoletaDtos.LineaDestrezaDto> destrezas = new ArrayList<>();

        if (nivel == NivelEducativo.INICIAL || nivel == NivelEducativo.PREPARATORIA) {
            destrezas = evaluacionDestrezaRepository.findByAlumnoIdAndPeriodoAcademicoId(alumnoId, periodoAcademicoId).stream()
                    .map(evaluacion -> new BoletaDtos.LineaDestrezaDto(
                            evaluacion.getAmbitoAprendizaje(), evaluacion.getDestreza(), evaluacion.getEscala().getCodigo()))
                    .toList();
        } else {
            for (MateriaCurso materiaCurso : materiaCursoRepository.findByCursoId(alumno.getCurso().getId())) {
                promedioMateriaPeriodoRepository
                        .findByAlumnoIdAndMateriaCursoIdAndPeriodoAcademicoId(alumnoId, materiaCurso.getId(), periodoAcademicoId)
                        .ifPresent(promedio -> materias.add(new BoletaDtos.LineaMateriaDto(
                                materiaCurso.getId(),
                                materiaCurso.getMateria().getNombre(),
                                promedio.getPromedioFinal(),
                                promedio.getEquivalenciaCualitativa() != null ? promedio.getEquivalenciaCualitativa().getCodigo() : null)));
            }
        }

        String comportamiento = evaluacionComportamentalRepository
                .findByAlumnoIdAndPeriodoAcademicoId(alumnoId, periodoAcademicoId)
                .map(EvaluacionComportamental::getDescripcionCualitativa)
                .orElse(null);

        return new BoletaDtos.InformeProgresoDto(
                alumnoId, alumno.getNombres() + " " + alumno.getApellidos(),
                periodoAcademicoId, materias, destrezas, comportamiento);
    }

    public BoletaDtos.InformeFinalAnualDto generarInformeFinalAnual(Long alumnoId, Long anioLectivoId) {
        Alumno alumno = alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno", alumnoId));
        Curso curso = alumno.getCurso();

        List<PeriodoAcademico> periodos = periodoAcademicoRepository.findByCursoIdOrderByNumeroAsc(curso.getId());
        List<MateriaCurso> ofertas = materiaCursoRepository.findByCursoId(curso.getId());

        List<BoletaDtos.LineaMateriaAnualDto> materias = new ArrayList<>();
        for (MateriaCurso materiaCurso : ofertas) {
            List<BigDecimal> promediosPorPeriodo = periodos.stream()
                    .map(periodo -> promedioMateriaPeriodoRepository
                            .findByAlumnoIdAndMateriaCursoIdAndPeriodoAcademicoId(alumnoId, materiaCurso.getId(), periodo.getId())
                            .map(PromedioMateriaPeriodo::getPromedioFinal)
                            .orElse(null))
                    .toList();

            // Convención: la evaluación supletoria se registra sobre el último
            // periodo académico del curso (posterior al cierre del año lectivo).
            BigDecimal supletoria = BigDecimal.ZERO;
            if (!periodos.isEmpty()) {
                Long ultimoPeriodoId = periodos.get(periodos.size() - 1).getId();
                supletoria = notaRepository
                        .findByAlumnoIdAndActividad_MateriaCursoIdAndActividad_PeriodoAcademicoId(alumnoId, materiaCurso.getId(), ultimoPeriodoId)
                        .stream()
                        .filter(nota -> nota.getActividad().getCaracter() == CaracterEvaluacion.SUPLETORIA)
                        .map(Nota::getCalificacion)
                        .findFirst()
                        .orElse(BigDecimal.ZERO);
            }

            BigDecimal promedioFinal = promedioService.calcularPromedioAnual(alumnoId, materiaCurso.getId());
            materias.add(new BoletaDtos.LineaMateriaAnualDto(
                    materiaCurso.getId(), materiaCurso.getMateria().getNombre(), promediosPorPeriodo, supletoria, promedioFinal));
        }

        BigDecimal sumaPromedios = materias.stream()
                .map(BoletaDtos.LineaMateriaAnualDto::promedioFinal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int cantidadMaterias = Math.max(1, materias.size());
        BigDecimal promedioGeneral = NotaService.truncar2Decimales(
                sumaPromedios.divide(new BigDecimal(cantidadMaterias), 10, RoundingMode.DOWN));

        List<String> comportamientoPorPeriodo = periodos.stream()
                .map(periodo -> evaluacionComportamentalRepository
                        .findByAlumnoIdAndPeriodoAcademicoId(alumnoId, periodo.getId())
                        .map(EvaluacionComportamental::getDescripcionCualitativa)
                        .orElse(null))
                .toList();

        String resultadoPromocion = registroPromocionRepository.findByAlumnoIdAndAnioLectivoId(alumnoId, anioLectivoId)
                .map(registro -> registro.getTipoPromocion().name())
                .orElse(TipoPromocion.PENDIENTE.name());

        return new BoletaDtos.InformeFinalAnualDto(
                alumnoId, alumno.getNombres() + " " + alumno.getApellidos(), anioLectivoId,
                materias, promedioGeneral, comportamientoPorPeriodo, resultadoPromocion);
    }
}
