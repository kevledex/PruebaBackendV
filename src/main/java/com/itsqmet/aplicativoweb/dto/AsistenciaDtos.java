package com.itsqmet.aplicativoweb.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.List;

/**
 * NUEVO: AsistenciaController usaba únicamente el CRUD individual heredado
 * del patrón de los demás controladores, pero el frontend registra la
 * asistencia de un curso completo en un solo envío (un aula, una fecha, una
 * materia, N estudiantes). Este DTO representa ese envío por lote.
 */
public final class AsistenciaDtos {

    private AsistenciaDtos() {
    }

    public record RegistroLote(
            @NotNull(message = "La fecha es obligatoria") LocalDate fecha,
            @NotNull(message = "La materia es obligatoria") Long materiaId,
            @NotEmpty(message = "Debe incluir al menos un estudiante") @Valid List<ItemAsistencia> estudiantes
    ) {
    }

    public record ItemAsistencia(
            @NotNull(message = "El alumno es obligatorio") Long alumnoId,
            @NotNull(message = "El estado es obligatorio")
            @Pattern(regexp = "Presente|Atraso|Falta Justificada|Falta Injustificada",
                    message = "El estado debe ser: Presente, Atraso, Falta Justificada o Falta Injustificada")
            String estado,
            String observacion
    ) {
    }
}
