package com.itsqmet.aplicativoweb.model;

import com.itsqmet.aplicativoweb.enums.EstadoAlerta;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * NUEVO. Art. 4.c y Art. 9 del Acuerdo Ministerial: identifica de forma
 * oportuna el bajo desempeño académico (promedio de evaluaciones formativas
 * entre 0.01 y 6.99) para activar refuerzo pedagógico y, de persistir,
 * derivar a evaluación psicopedagógica.
 */
@Entity
@Data
@NoArgsConstructor
public class AlertaBajoRendimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El alumno es obligatorio")
    @ManyToOne(optional = false)
    private Alumno alumno;

    @NotNull(message = "La materia-curso es obligatoria")
    @ManyToOne(optional = false)
    private MateriaCurso materiaCurso;

    @NotNull(message = "El periodo académico es obligatorio")
    @ManyToOne(optional = false)
    private PeriodoAcademico periodoAcademico;

    @NotNull(message = "La fecha de detección es obligatoria")
    private LocalDate fechaDeteccion;

    @NotNull(message = "El promedio detectado es obligatorio")
    @DecimalMin(value = "0.01", message = "El rango de alerta temprana inicia en 0.01 (Art. 9 del Acuerdo)")
    @DecimalMax(value = "6.99", message = "El rango de alerta temprana termina en 6.99 (Art. 9 del Acuerdo)")
    private BigDecimal promedioDetectado;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EstadoAlerta estado = EstadoAlerta.DETECTADA;

    private LocalDate fechaNotificacionRepresentante;

    @Column(length = 1000)
    private String observaciones;

    @ManyToOne
    private RefuerzoPedagogico refuerzoAsociado;

    @ManyToOne
    private EvaluacionPsicopedagogica derivacion;
}
