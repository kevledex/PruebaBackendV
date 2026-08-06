package com.itsqmet.aplicativoweb.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * NUEVO: DocenteController ya importaba
 * "com.itsqmet.aplicativoweb.dto.DocenteDtos.AsignacionMaterias", pero la
 * clase no existía en el proyecto. Representa la solicitud para asignar a
 * un docente una o varias ofertas de materia por curso (MateriaCurso).
 */
public final class DocenteDtos {

    private DocenteDtos() {
    }

    public record AsignacionMaterias(
            @NotEmpty(message = "Debe indicar al menos una materia-curso a asignar")
            List<Long> materiaCursoIds
    ) {}
}
