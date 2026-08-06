package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.enums.EstadoAlerta;
import com.itsqmet.aplicativoweb.enums.TipoEvaluacion;
import com.itsqmet.aplicativoweb.exception.RecursoNoEncontradoException;
import com.itsqmet.aplicativoweb.model.AlertaBajoRendimiento;
import com.itsqmet.aplicativoweb.model.EvaluacionPsicopedagogica;
import com.itsqmet.aplicativoweb.model.Nota;
import com.itsqmet.aplicativoweb.model.RefuerzoPedagogico;
import com.itsqmet.aplicativoweb.repository.AlertaBajoRendimientoRepository;
import com.itsqmet.aplicativoweb.repository.EvaluacionPsicopedagogicaRepository;
import com.itsqmet.aplicativoweb.repository.NotaRepository;
import com.itsqmet.aplicativoweb.repository.RefuerzoPedagogicoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * NUEVO. Implementa los 5 pasos del Art. 9 del Acuerdo Ministerial:
 * (1) analizar evaluaciones formativas 0.01-6.99, (2) identificar
 * alumno/grupo, (3) informar al representante, (4) activar refuerzo
 * pedagógico, (5) [el promedio con el refuerzo lo recalcula PromedioService
 * al reevaluar]. Si el bajo rendimiento persiste, deriva a evaluación
 * psicopedagógica.
 */
@Service
public class AlertaBajoRendimientoService {

    private static final BigDecimal LIMITE_INFERIOR = new BigDecimal("0.01");
    private static final BigDecimal LIMITE_SUPERIOR = new BigDecimal("6.99");

    private final AlertaBajoRendimientoRepository alertaBajoRendimientoRepository;
    private final NotaRepository notaRepository;
    private final RefuerzoPedagogicoRepository refuerzoPedagogicoRepository;
    private final EvaluacionPsicopedagogicaRepository evaluacionPsicopedagogicaRepository;

    public AlertaBajoRendimientoService(AlertaBajoRendimientoRepository alertaBajoRendimientoRepository,
                                         NotaRepository notaRepository,
                                         RefuerzoPedagogicoRepository refuerzoPedagogicoRepository,
                                         EvaluacionPsicopedagogicaRepository evaluacionPsicopedagogicaRepository) {
        this.alertaBajoRendimientoRepository = alertaBajoRendimientoRepository;
        this.notaRepository = notaRepository;
        this.refuerzoPedagogicoRepository = refuerzoPedagogicoRepository;
        this.evaluacionPsicopedagogicaRepository = evaluacionPsicopedagogicaRepository;
    }

    public List<AlertaBajoRendimiento> listar() {
        return alertaBajoRendimientoRepository.findAll();
    }

    public List<AlertaBajoRendimiento> listarPorAlumno(Long alumnoId) {
        return alertaBajoRendimientoRepository.findByAlumnoId(alumnoId);
    }

    public AlertaBajoRendimiento obtenerPorId(Long id) {
        return alertaBajoRendimientoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("AlertaBajoRendimiento", id));
    }

    public List<AlertaBajoRendimiento> detectarAlertas(Long periodoAcademicoId) {
        List<Nota> formativasDelPeriodo = notaRepository.findByActividad_PeriodoAcademicoId(periodoAcademicoId).stream()
                .filter(nota -> nota.getActividad().getTipoEvaluacion() == TipoEvaluacion.FORMATIVA)
                .toList();

        Map<String, List<Nota>> agrupadasPorAlumnoYMateria = formativasDelPeriodo.stream()
                .collect(Collectors.groupingBy(nota ->
                        nota.getAlumno().getId() + "-" + nota.getActividad().getMateriaCurso().getId()));

        List<AlertaBajoRendimiento> nuevasAlertas = new ArrayList<>();
        for (List<Nota> grupo : agrupadasPorAlumnoYMateria.values()) {
            BigDecimal promedio = promedioSimple(grupo.stream().map(Nota::getCalificacion).toList());
            boolean enRangoDeAlerta = promedio.compareTo(LIMITE_INFERIOR) >= 0 && promedio.compareTo(LIMITE_SUPERIOR) <= 0;
            if (!enRangoDeAlerta) {
                continue;
            }

            Long alumnoId = grupo.get(0).getAlumno().getId();
            Long materiaCursoId = grupo.get(0).getActividad().getMateriaCurso().getId();
            boolean yaExisteAlertaAbierta = alertaBajoRendimientoRepository
                    .findByAlumnoIdAndMateriaCursoIdAndPeriodoAcademicoIdAndEstadoNot(
                            alumnoId, materiaCursoId, periodoAcademicoId, EstadoAlerta.RESUELTA)
                    .isPresent();
            if (yaExisteAlertaAbierta) {
                continue;
            }

            AlertaBajoRendimiento alerta = new AlertaBajoRendimiento();
            alerta.setAlumno(grupo.get(0).getAlumno());
            alerta.setMateriaCurso(grupo.get(0).getActividad().getMateriaCurso());
            alerta.setPeriodoAcademico(grupo.get(0).getActividad().getPeriodoAcademico());
            alerta.setFechaDeteccion(LocalDate.now());
            alerta.setPromedioDetectado(promedio);
            alerta.setEstado(EstadoAlerta.DETECTADA);
            nuevasAlertas.add(alertaBajoRendimientoRepository.save(alerta));
        }
        return nuevasAlertas;
    }

    public AlertaBajoRendimiento notificarRepresentante(Long id) {
        AlertaBajoRendimiento alerta = obtenerPorId(id);
        alerta.setEstado(EstadoAlerta.NOTIFICADA_REPRESENTANTE);
        alerta.setFechaNotificacionRepresentante(LocalDate.now());
        return alertaBajoRendimientoRepository.save(alerta);
    }

    public AlertaBajoRendimiento iniciarRefuerzo(Long alertaId, Long refuerzoId) {
        AlertaBajoRendimiento alerta = obtenerPorId(alertaId);
        RefuerzoPedagogico refuerzo = refuerzoPedagogicoRepository.findById(refuerzoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("RefuerzoPedagogico", refuerzoId));
        alerta.setRefuerzoAsociado(refuerzo);
        alerta.setEstado(EstadoAlerta.EN_REFUERZO);
        return alertaBajoRendimientoRepository.save(alerta);
    }

    public AlertaBajoRendimiento resolver(Long id) {
        AlertaBajoRendimiento alerta = obtenerPorId(id);
        alerta.setEstado(EstadoAlerta.RESUELTA);
        return alertaBajoRendimientoRepository.save(alerta);
    }

    public AlertaBajoRendimiento derivarPsicopedagogico(Long alertaId, Long evaluacionPsicopedagogicaId) {
        AlertaBajoRendimiento alerta = obtenerPorId(alertaId);
        EvaluacionPsicopedagogica derivacion = evaluacionPsicopedagogicaRepository.findById(evaluacionPsicopedagogicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("EvaluacionPsicopedagogica", evaluacionPsicopedagogicaId));
        alerta.setDerivacion(derivacion);
        alerta.setEstado(EstadoAlerta.DERIVADA_PSICOPEDAGOGICO);
        return alertaBajoRendimientoRepository.save(alerta);
    }

    private BigDecimal promedioSimple(List<BigDecimal> valores) {
        if (valores.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.DOWN);
        }
        BigDecimal suma = valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return suma.divide(new BigDecimal(valores.size()), 2, RoundingMode.DOWN);
    }
}
