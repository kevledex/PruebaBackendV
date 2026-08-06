package com.itsqmet.aplicativoweb.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * NUEVO. Representa un periodo académico concreto (trimestre/quimestre/
 * bimestre) dentro de un Curso (Art. 4.b del Acuerdo Ministerial). Sustituye
 * el campo "periodo" de texto libre (validado con un regex "Q1|Q2|Quimestre
 * 1|Quimestre 2") que tenía la entidad Actividad, el cual además asumía
 * organización quimestral por defecto -- contradiciendo que el sostenimiento
 * fiscal es trimestral (Cap. 2, pág. 5 del Instructivo).
 */
@Entity
@Data
@NoArgsConstructor
public class PeriodoAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El curso es obligatorio")
    @ManyToOne(optional = false)
    private Curso curso;

    @Min(value = 1, message = "El número de periodo debe ser al menos 1")
    @Max(value = 4, message = "El número de periodo no puede ser mayor a 4")
    private int numero; // 1,2,3 (trimestres) o 1,2 (quimestres) etc.

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    /**
     * Una vez cerrado no deberían crearse más Actividad/Nota sobre este
     * periodo; ActividadService debe validar este flag antes de aceptar
     * nuevas evaluaciones (ver paquete de ajustes a Actividad).
     */
    private boolean cerrado;
}
