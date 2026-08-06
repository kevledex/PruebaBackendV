package com.itsqmet.aplicativoweb.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * NUEVO. Art. 27 del Acuerdo Ministerial: evaluación diagnóstica de aspectos
 * socioemocionales, obligatoria durante el primer mes del año/ciclo lectivo
 * en todas las instituciones educativas (Anexo 1 del Instructivo).
 */
@Entity
@Data
@NoArgsConstructor
public class EvaluacionDiagnosticaSocioemocional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El alumno es obligatorio")
    @ManyToOne(optional = false)
    private Alumno alumno;

    @NotNull(message = "El año lectivo es obligatorio")
    @ManyToOne(optional = false)
    private AnioLectivo anioLectivo;

    @NotNull(message = "La fecha de aplicación es obligatoria")
    private LocalDate fechaAplicacion;

    @NotBlank(message = "Los resultados son obligatorios")
    @Column(length = 2000)
    private String resultados;
}
