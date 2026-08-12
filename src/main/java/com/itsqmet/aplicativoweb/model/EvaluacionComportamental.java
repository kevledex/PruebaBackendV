package com.itsqmet.aplicativoweb.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * NUEVO. Cap. IX (Art. 26-28) del Acuerdo Ministerial: descripción
 * cualitativa del comportamiento, resultado de la evaluación socioemocional.
 * Aparece como fila obligatoria en TODAS las boletas de ejemplo del
 * Instructivo (Tablas 24-36). Nunca es requisito de promoción (pág. 16 del
 * Instructivo).
 */
@Entity
@Data
@NoArgsConstructor
public class EvaluacionComportamental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El alumno es obligatorio")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Alumno alumno;

    @NotNull(message = "El periodo académico es obligatorio")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private PeriodoAcademico periodoAcademico;

    @NotNull(message = "El docente es obligatorio")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Docente docente;

    @NotBlank(message = "La descripción cualitativa es obligatoria")
    @Column(length = 500)
    private String descripcionCualitativa;

    private boolean cuentaParaPromocion = false;
}
