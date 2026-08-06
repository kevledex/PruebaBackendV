package com.itsqmet.aplicativoweb.dto;

/**
 * NUEVO. Cupos de mejora de calificación usados/disponibles por un alumno
 * en una materia y año lectivo (Art. 11 y 12 del Acuerdo Ministerial). La
 * normativa exige comunicar esta información al estudiantado: "es
 * importante que el estudiantado utilice, de forma responsable, el máximo
 * establecido" (pág. 31 y 33 del Instructivo).
 */
public final class MejoraDtos {

    private MejoraDtos() {
    }

    public record CupoMejora(
            Long alumnoId,
            Long materiaCursoId,
            int mejorasDirectasUsadasMateriaPeriodo,
            int mejorasDirectasUsadasAnio,
            int mejorasConRefuerzoUsadasMateria,
            int mejorasConRefuerzoUsadasAnio,
            int mejorasDirectasDisponiblesAnio,
            int mejorasConRefuerzoDisponiblesAnio
    ) {}
}
