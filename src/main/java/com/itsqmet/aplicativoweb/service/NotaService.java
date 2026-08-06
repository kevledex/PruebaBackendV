package com.itsqmet.aplicativoweb.service;


import com.itsqmet.aplicativoweb.exception.NotaNoEncontradaException;
import com.itsqmet.aplicativoweb.exception.DatosInvalidosException;
import com.itsqmet.aplicativoweb.model.Nota;
import com.itsqmet.aplicativoweb.repository.NotaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


/**
 * AJUSTADO: ahora exige "actividad" (antes solo se validaban calificación y
 * fecha; sin actividad la nota no puede clasificarse como
 * diagnóstica/formativa/sumativa ni ubicarse en un periodo académico). Se
 * agrega "truncar2Decimales", utilitario reutilizado por PromedioService,
 * que aplica la regla del Cap. 2.3 (pág. 15) del Instructivo: los promedios
 * se truncan a 2 decimales SIN redondear (RoundingMode.DOWN, no HALF_UP).
 */
@Service
public class NotaService {

    private final NotaRepository notaRepository;

    public NotaService(NotaRepository notaRepository) {
        this.notaRepository = notaRepository;
    }

    //READ- listar notas
    public List<Nota> obtenerTodo() {
        return notaRepository.findAll();
    }

    //READ- buscar por ID con excepción personalizada
    public Nota buscarPorId(Long id) {
        return notaRepository.findById(id)
                .orElseThrow(() -> new NotaNoEncontradaException(id));
    }

    //CREATE- crear nota con validación
    public Nota crearNota(Nota nota) {
        if (nota.getCalificacion() == null || nota.getFecha() == null) {
            throw new DatosInvalidosException("La calificación y la fecha son obligatorios");
        }
        if (nota.getActividad() == null) {
            throw new DatosInvalidosException("La actividad es obligatoria");
        }
        nota.setCalificacion(truncar2Decimales(nota.getCalificacion()));
        return notaRepository.save(nota);
    }

    //UPDATE- actualizar nota
    public Nota actualizar(Long id, Nota notaActualizada) {
        return notaRepository.findById(id).map(nota -> {
            nota.setCalificacion(truncar2Decimales(notaActualizada.getCalificacion()));
            nota.setFecha(notaActualizada.getFecha());
            nota.setObservacion(notaActualizada.getObservacion());
            return notaRepository.save(nota);
        }).orElseThrow(() -> new NotaNoEncontradaException(id));
    }

    //DELETE- eliminar nota
    public boolean eliminar(Long id) {
        if (!notaRepository.existsById(id)) {
            throw new NotaNoEncontradaException(id);
        }
        notaRepository.deleteById(id);
        return true;
    }

    /**
     * Trunca (sin redondear) a 2 decimales, tal como exige el Instructivo
     * (pág. 15): "El cálculo de los promedios utilizará dos lugares
     * decimales truncados, sin redondeos".
     */
    public static BigDecimal truncar2Decimales(BigDecimal valor) {
        return valor == null ? null : valor.setScale(2, RoundingMode.DOWN);
    }
}
