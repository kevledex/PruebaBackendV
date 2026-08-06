package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.exception.OperacionNoPermitidaException;
import com.itsqmet.aplicativoweb.exception.RecursoNoEncontradoException;
import com.itsqmet.aplicativoweb.model.AnioLectivo;
import com.itsqmet.aplicativoweb.repository.AnioLectivoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * NUEVO. Gestiona el año lectivo (Art. 4.a del Acuerdo Ministerial). Solo
 * puede existir un AnioLectivo activo a la vez, que es el que acota los
 * topes de mejora de calificaciones "dentro del mismo año lectivo"
 * (Art. 11 y 12).
 */
@Service
public class AnioLectivoService {

    private final AnioLectivoRepository anioLectivoRepository;

    public AnioLectivoService(AnioLectivoRepository anioLectivoRepository) {
        this.anioLectivoRepository = anioLectivoRepository;
    }

    public List<AnioLectivo> listar() {
        return anioLectivoRepository.findAll();
    }

    public AnioLectivo obtenerPorId(Long id) {
        return anioLectivoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Año lectivo", id));
    }

    public AnioLectivo obtenerActivo() {
        return anioLectivoRepository.findByActivoTrue()
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un año lectivo activo"));
    }

    public AnioLectivo crear(AnioLectivo anioLectivo) {
        anioLectivo.setActivo(false); // se activa explícitamente vía activar(id)
        return anioLectivoRepository.save(anioLectivo);
    }

    public AnioLectivo actualizar(Long id, AnioLectivo datos) {
        AnioLectivo actual = obtenerPorId(id);
        actual.setNombre(datos.getNombre());
        actual.setFechaInicio(datos.getFechaInicio());
        actual.setFechaFin(datos.getFechaFin());
        return anioLectivoRepository.save(actual);
    }

    public AnioLectivo activar(Long id) {
        AnioLectivo nuevo = obtenerPorId(id);
        anioLectivoRepository.findAllByActivoTrue().forEach(anio -> {
            anio.setActivo(false);
            anioLectivoRepository.save(anio);
        });
        nuevo.setActivo(true);
        return anioLectivoRepository.save(nuevo);
    }

    public void eliminar(Long id) {
        AnioLectivo anioLectivo = obtenerPorId(id);
        if (anioLectivo.isActivo()) {
            throw new OperacionNoPermitidaException("No se puede eliminar el año lectivo activo");
        }
        anioLectivoRepository.deleteById(id);
    }
}
