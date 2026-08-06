package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.enums.CategoriaInsumoMedia;
import com.itsqmet.aplicativoweb.enums.EscalaCualitativa;
import com.itsqmet.aplicativoweb.enums.NivelEducativo;
import com.itsqmet.aplicativoweb.exception.RecursoNoEncontradoException;
import com.itsqmet.aplicativoweb.model.MateriaCurso;
import com.itsqmet.aplicativoweb.model.Nota;
import com.itsqmet.aplicativoweb.model.PeriodoAcademico;
import com.itsqmet.aplicativoweb.model.PromedioMateriaPeriodo;
import com.itsqmet.aplicativoweb.repository.MateriaCursoRepository;
import com.itsqmet.aplicativoweb.repository.NotaRepository;
import com.itsqmet.aplicativoweb.repository.PeriodoAcademicoRepository;
import com.itsqmet.aplicativoweb.repository.PromedioMateriaPeriodoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * NUEVO. Es el servicio central que faltaba por completo en el backend
 * original: sin él, ningún reporte, alerta, mejora, supletoria o promoción
 * puede calcularse.
 *
 * Reglas aplicadas (Cap. XII y XIII del Acuerdo Ministerial; Cap. 2 del
 * Instructivo). Esta institución solo llega hasta 7mo EGB (Subnivel Media),
 * por lo que no existen los casos de Superior ni Bachillerato:
 *  - Inicial/Preparatoria: no aplica (se maneja vía EvaluacionDestreza, sin
 *    promedio -- pág. 6 del Instructivo).
 *  - Subnivel Elemental: promedio simple de los aportes, truncado a 2
 *    decimales sin redondear (Tabla 6 del Instructivo).
 *  - Subnivel Media: cálculo jerárquico en tres niveles, replicando el
 *    orden real de las planillas de la institución (Cap. 2.3, pág. 15;
 *    Tabla 9-10 del Instructivo), con BigDecimal y truncamiento
 *    (RoundingMode.DOWN, nunca redondeo) en cada nivel:
 *      Nivel 1: promedio simple de Tareas, Individuales, Lecciones y
 *               Grupales (cada sub-categoría de la evaluación formativa).
 *      Nivel 2: INSUMO_1 = (Tareas+Individuales+Lecciones)/3,
 *               INSUMO_2 = Grupales,
 *               EVALUACION_FORMATIVA_TOTAL = (INSUMO_1+INSUMO_2)/2.
 *      Nivel 3: P1 = Formativa*0.70, P2 = Proyecto*0.15, P3 = Examen*0.15,
 *               PROMEDIO_TRIMESTRAL = P1+P2+P3.
 *    Ver PromedioService.calcularJerarquicoMedia().
 *  - El promedio anual es el promedio simple de los promedios de cada
 *    periodo académico cerrado (Art. 34 del Acuerdo).
 */
@Service
public class PromedioService {

    private static final BigDecimal PESO_FORMATIVA = new BigDecimal("0.70");
    private static final BigDecimal PESO_PROYECTO = new BigDecimal("0.15");
    private static final BigDecimal PESO_EXAMEN = new BigDecimal("0.15");
    private static final BigDecimal TRES = new BigDecimal("3");
    private static final BigDecimal DOS = new BigDecimal("2");

    private final NotaRepository notaRepository;
    private final PromedioMateriaPeriodoRepository promedioMateriaPeriodoRepository;
    private final MateriaCursoRepository materiaCursoRepository;
    private final PeriodoAcademicoRepository periodoAcademicoRepository;
    private final EscalaCualitativaMapper escalaCualitativaMapper;

    public PromedioService(NotaRepository notaRepository,
                            PromedioMateriaPeriodoRepository promedioMateriaPeriodoRepository,
                            MateriaCursoRepository materiaCursoRepository,
                            PeriodoAcademicoRepository periodoAcademicoRepository,
                            EscalaCualitativaMapper escalaCualitativaMapper) {
        this.notaRepository = notaRepository;
        this.promedioMateriaPeriodoRepository = promedioMateriaPeriodoRepository;
        this.materiaCursoRepository = materiaCursoRepository;
        this.periodoAcademicoRepository = periodoAcademicoRepository;
        this.escalaCualitativaMapper = escalaCualitativaMapper;
    }

    /**
     * Calcula (o recalcula) el promedio de una materia en un periodo
     * académico concreto para un alumno. Devuelve null si el nivel
     * educativo del curso es INICIAL o PREPARATORIA (no aplica promedio).
     */
    public PromedioMateriaPeriodo calcularPromedioPeriodo(Long alumnoId, Long materiaCursoId, Long periodoAcademicoId) {
        MateriaCurso materiaCurso = materiaCursoRepository.findById(materiaCursoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("MateriaCurso", materiaCursoId));

        NivelEducativo nivel = materiaCurso.getCurso().getNivel();
        if (nivel == NivelEducativo.INICIAL || nivel == NivelEducativo.PREPARATORIA) {
            return null;
        }

        List<Nota> notas = notaRepository.findByAlumnoIdAndActividad_MateriaCursoIdAndActividad_PeriodoAcademicoId(
                alumnoId, materiaCursoId, periodoAcademicoId);
        if (notas.isEmpty()) {
            throw new RecursoNoEncontradoException(
                    "No existen notas registradas para calcular el promedio de esa materia y periodo");
        }

        PeriodoAcademico periodoAcademico = periodoAcademicoRepository.findById(periodoAcademicoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Periodo académico", periodoAcademicoId));

        PromedioMateriaPeriodo promedio = promedioMateriaPeriodoRepository
                .findByAlumnoIdAndMateriaCursoIdAndPeriodoAcademicoId(alumnoId, materiaCursoId, periodoAcademicoId)
                .orElseGet(PromedioMateriaPeriodo::new);
        promedio.setAlumno(notas.get(0).getAlumno());
        promedio.setMateriaCurso(materiaCurso);
        promedio.setPeriodoAcademico(periodoAcademico);

        BigDecimal promedioFinal;

        if (nivel == NivelEducativo.ELEMENTAL) {
            promedioFinal = promedioSimple(notas.stream().map(Nota::getCalificacion).toList());
            promedio.setPromedioFormativa(null);
            promedio.setPromedioSumativa(null);
            limpiarDesgloseMedia(promedio);
            promedio.setPromedioFinal(promedioFinal);
        } else if (nivel == NivelEducativo.MEDIA) {
            promedioFinal = calcularJerarquicoMedia(notas, promedio);
        } else {
            throw new IllegalStateException("Nivel educativo sin regla de promedio definida: " + nivel);
        }

        EscalaCualitativa equivalencia = escalaCualitativaMapper.desdeNumero(promedioFinal);
        promedio.setEquivalenciaCualitativa(equivalencia);

        return promedioMateriaPeriodoRepository.save(promedio);
    }

    /**
     * Cálculo jerárquico de EGB Media (Cap. 2.3 del Instructivo, replicando
     * el orden real de las planillas de la institución), truncando a 2
     * decimales SIN redondear en cada nivel:
     *
     * Nivel 1 (sub-categorías formativas, cada una promedio simple truncado):
     *   Prom_Tareas, Prom_Individuales, Prom_Lecciones, Prom_Grupales
     * Nivel 2 (insumos formativos):
     *   INSUMO_1 = (Prom_Tareas + Prom_Individuales + Prom_Lecciones) / 3
     *   INSUMO_2 = Prom_Grupales
     *   EVALUACION_FORMATIVA_TOTAL = (INSUMO_1 + INSUMO_2) / 2
     * Nivel 3 (ponderación final del periodo):
     *   P1 = EVALUACION_FORMATIVA_TOTAL * 0.70
     *   P2 = NotaProyecto * 0.15
     *   P3 = NotaExamen * 0.15
     *   PROMEDIO_TRIMESTRAL = P1 + P2 + P3
     */
    private BigDecimal calcularJerarquicoMedia(List<Nota> notas, PromedioMateriaPeriodo promedio) {
        BigDecimal promedioTareas = promedioSimple(notasDeCategoria(notas, CategoriaInsumoMedia.TAREA));
        BigDecimal promedioIndividuales = promedioSimple(notasDeCategoria(notas, CategoriaInsumoMedia.INDIVIDUAL));
        BigDecimal promedioLecciones = promedioSimple(notasDeCategoria(notas, CategoriaInsumoMedia.LECCION));
        BigDecimal promedioGrupales = promedioSimple(notasDeCategoria(notas, CategoriaInsumoMedia.GRUPAL));

        BigDecimal insumo1 = NotaService.truncar2Decimales(
                promedioTareas.add(promedioIndividuales).add(promedioLecciones)
                        .divide(TRES, 10, RoundingMode.DOWN));
        BigDecimal insumo2 = promedioGrupales;
        BigDecimal evaluacionFormativaTotal = NotaService.truncar2Decimales(
                insumo1.add(insumo2).divide(DOS, 10, RoundingMode.DOWN));

        BigDecimal notaProyecto = promedioSimple(notasDeCategoria(notas, CategoriaInsumoMedia.PROYECTO));
        BigDecimal notaExamen = promedioSimple(notasDeCategoria(notas, CategoriaInsumoMedia.EXAMEN));

        BigDecimal p1 = NotaService.truncar2Decimales(evaluacionFormativaTotal.multiply(PESO_FORMATIVA));
        BigDecimal p2 = NotaService.truncar2Decimales(notaProyecto.multiply(PESO_PROYECTO));
        BigDecimal p3 = NotaService.truncar2Decimales(notaExamen.multiply(PESO_EXAMEN));
        BigDecimal promedioTrimestral = NotaService.truncar2Decimales(p1.add(p2).add(p3));

        promedio.setPromedioTareas(promedioTareas);
        promedio.setPromedioIndividuales(promedioIndividuales);
        promedio.setPromedioLecciones(promedioLecciones);
        promedio.setPromedioGrupales(promedioGrupales);
        promedio.setInsumo1(insumo1);
        promedio.setInsumo2(insumo2);
        promedio.setPromedioFormativa(evaluacionFormativaTotal);
        promedio.setNotaProyecto(notaProyecto);
        promedio.setNotaExamen(notaExamen);
        promedio.setP1Formativo(p1);
        promedio.setP2Proyecto(p2);
        promedio.setP3Examen(p3);
        promedio.setPromedioSumativa(null);
        promedio.setPromedioFinal(promedioTrimestral);

        return promedioTrimestral;
    }

    private List<BigDecimal> notasDeCategoria(List<Nota> notas, CategoriaInsumoMedia categoria) {
        return notas.stream()
                .filter(nota -> nota.getActividad().getCategoriaInsumoMedia() == categoria)
                .map(Nota::getCalificacion)
                .toList();
    }

    private void limpiarDesgloseMedia(PromedioMateriaPeriodo promedio) {
        promedio.setPromedioTareas(null);
        promedio.setPromedioIndividuales(null);
        promedio.setPromedioLecciones(null);
        promedio.setPromedioGrupales(null);
        promedio.setInsumo1(null);
        promedio.setInsumo2(null);
        promedio.setNotaProyecto(null);
        promedio.setNotaExamen(null);
        promedio.setP1Formativo(null);
        promedio.setP2Proyecto(null);
        promedio.setP3Examen(null);
    }

    /**
     * Recalcula el promedio de todas las combinaciones alumno+materia que
     * tengan notas registradas dentro de un periodo académico. Se invoca
     * normalmente después de cerrar el periodo (ver PeriodoAcademicoService).
     */
    public List<PromedioMateriaPeriodo> recalcularPeriodo(Long periodoAcademicoId) {
        List<Nota> notasDelPeriodo = notaRepository.findByActividad_PeriodoAcademicoId(periodoAcademicoId);

        var combinaciones = notasDelPeriodo.stream()
                .collect(Collectors.groupingBy(nota ->
                        nota.getAlumno().getId() + "-" + nota.getActividad().getMateriaCurso().getId()));

        List<PromedioMateriaPeriodo> resultado = new ArrayList<>();
        for (List<Nota> grupo : combinaciones.values()) {
            Long alumnoId = grupo.get(0).getAlumno().getId();
            Long materiaCursoId = grupo.get(0).getActividad().getMateriaCurso().getId();
            PromedioMateriaPeriodo calculado = calcularPromedioPeriodo(alumnoId, materiaCursoId, periodoAcademicoId);
            if (calculado != null) {
                resultado.add(calculado);
            }
        }
        return resultado;
    }

    /**
     * Promedio anual: promedio simple de los promedios de cada periodo
     * académico ya cerrado (Art. 34 del Acuerdo: "sumatoria de la nota
     * final de cada periodo académico dividido para tres [o dos]").
     */
    public BigDecimal calcularPromedioAnual(Long alumnoId, Long materiaCursoId) {
        MateriaCurso materiaCurso = materiaCursoRepository.findById(materiaCursoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("MateriaCurso", materiaCursoId));

        List<PeriodoAcademico> periodosCerrados = periodoAcademicoRepository
                .findByCursoIdOrderByNumeroAsc(materiaCurso.getCurso().getId())
                .stream().filter(PeriodoAcademico::isCerrado).toList();

        List<BigDecimal> promediosPorPeriodo = periodosCerrados.stream()
                .map(periodo -> promedioMateriaPeriodoRepository
                        .findByAlumnoIdAndMateriaCursoIdAndPeriodoAcademicoId(alumnoId, materiaCursoId, periodo.getId())
                        .map(PromedioMateriaPeriodo::getPromedioFinal)
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();

        return promedioSimple(promediosPorPeriodo);
    }

    private BigDecimal promedioSimple(List<BigDecimal> valores) {
        if (valores.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.DOWN);
        }
        BigDecimal suma = valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal promedio = suma.divide(new BigDecimal(valores.size()), 10, RoundingMode.DOWN);
        return NotaService.truncar2Decimales(promedio);
    }
}
