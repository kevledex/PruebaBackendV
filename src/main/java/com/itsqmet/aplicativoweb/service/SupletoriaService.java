package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.enums.CaracterEvaluacion;
import com.itsqmet.aplicativoweb.enums.EstadoRefuerzo;
import com.itsqmet.aplicativoweb.exception.OperacionNoPermitidaException;
import com.itsqmet.aplicativoweb.exception.RecursoNoEncontradoException;
import com.itsqmet.aplicativoweb.model.MateriaCurso;
import com.itsqmet.aplicativoweb.model.Nota;
import com.itsqmet.aplicativoweb.model.RefuerzoPedagogico;
import com.itsqmet.aplicativoweb.repository.MateriaCursoRepository;
import com.itsqmet.aplicativoweb.repository.NotaRepository;
import com.itsqmet.aplicativoweb.repository.RefuerzoPedagogicoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * NUEVO. Implementa la evaluación supletoria (Art. 21 y 22 del Acuerdo
 * Ministerial; pág. 35-36 del Instructivo), que antes no existía:
 *  - Elegible cuando el promedio final anual de la materia está entre 4.01
 *    y 6.99.
 *  - Las asignaturas optativas/adicionales NO son objeto de supletoria
 *    (pág. 36 del Instructivo).
 *  - Requiere un RefuerzoPedagogico FINALIZADO (refuerzo académico previo
 *    de máximo 5 días).
 *  - La nota final que se registra queda topada en 7.00 aunque el
 *    estudiante haya obtenido más (Art. 21: "La máxima nota para este tipo
 *    de evaluación supletoria será de 7,00 puntos"; ejemplo exacto en la
 *    Tabla 23 del Instructivo, pág. 36).
 */
@Service
public class SupletoriaService {

    private static final BigDecimal TOPE_SUPLETORIA = new BigDecimal("7.00");
    private static final BigDecimal LIMITE_INFERIOR = new BigDecimal("4.01");
    private static final BigDecimal LIMITE_SUPERIOR = new BigDecimal("6.99");

    private final PromedioService promedioService;
    private final NotaRepository notaRepository;
    private final RefuerzoPedagogicoRepository refuerzoPedagogicoRepository;
    private final MateriaCursoRepository materiaCursoRepository;

    public SupletoriaService(PromedioService promedioService,
                              NotaRepository notaRepository,
                              RefuerzoPedagogicoRepository refuerzoPedagogicoRepository,
                              MateriaCursoRepository materiaCursoRepository) {
        this.promedioService = promedioService;
        this.notaRepository = notaRepository;
        this.refuerzoPedagogicoRepository = refuerzoPedagogicoRepository;
        this.materiaCursoRepository = materiaCursoRepository;
    }

    public boolean esElegible(Long alumnoId, Long materiaCursoId) {
        MateriaCurso materiaCurso = materiaCursoRepository.findById(materiaCursoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("MateriaCurso", materiaCursoId));
        if (materiaCurso.getMateria().isEsOptativaOAdicional()) {
            return false;
        }
        BigDecimal promedioAnual = promedioService.calcularPromedioAnual(alumnoId, materiaCursoId);
        return promedioAnual.compareTo(LIMITE_INFERIOR) >= 0 && promedioAnual.compareTo(LIMITE_SUPERIOR) <= 0;
    }

    public Nota registrarSupletoria(Long alumnoId, Long refuerzoPedagogicoId, Nota notaSupletoria) {
        Long materiaCursoId = notaSupletoria.getActividad().getMateriaCurso().getId();

        if (!esElegible(alumnoId, materiaCursoId)) {
            throw new OperacionNoPermitidaException(
                    "El estudiante no es elegible para supletoria en esta materia (se requiere promedio final anual "
                            + "entre 4.01 y 6.99, Art. 22 del Acuerdo Ministerial, y que la materia no sea optativa)");
        }

        RefuerzoPedagogico refuerzo = refuerzoPedagogicoRepository.findById(refuerzoPedagogicoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("RefuerzoPedagogico", refuerzoPedagogicoId));
        if (refuerzo.getEstado() != EstadoRefuerzo.FINALIZADO) {
            throw new OperacionNoPermitidaException(
                    "El refuerzo académico previo a la supletoria debe estar FINALIZADO (pág. 36 del Instructivo)");
        }

        notaSupletoria.getActividad().setCaracter(CaracterEvaluacion.SUPLETORIA);

        BigDecimal calificacionCruda = NotaService.truncar2Decimales(notaSupletoria.getCalificacion());
        notaSupletoria.setCalificacionSupletoriaCruda(calificacionCruda);
        // Art. 21 del Acuerdo: tope de 7.00 aunque el estudiante haya obtenido más (Tabla 23 del Instructivo).
        notaSupletoria.setCalificacion(calificacionCruda.min(TOPE_SUPLETORIA));

        return notaRepository.save(notaSupletoria);
    }
}
