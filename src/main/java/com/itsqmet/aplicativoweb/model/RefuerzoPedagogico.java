package com.itsqmet.aplicativoweb.model;

import com.itsqmet.aplicativoweb.enums.EstadoRefuerzo;
import com.itsqmet.aplicativoweb.enums.OrigenRefuerzo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * NUEVO. Art. 3.j del Acuerdo Ministerial. Es prerrequisito explícito de
 * tres procesos distintos: alerta temprana (Art. 9), mejora de
 * calificaciones con refuerzo (Art. 12: "el docente aplicará estrategias de
 * refuerzo... dentro de los cinco días posteriores a la recepción de la
 * solicitud") y evaluación supletoria (pág. 36 del Instructivo: refuerzo de
 * 5 días previo, planificado y aprobado por la autoridad antes de
 * ejecutarse).
 */
@Entity
@Data
@NoArgsConstructor
public class RefuerzoPedagogico {

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

    @NotNull(message = "El docente responsable es obligatorio")
    @ManyToOne(optional = false)
    private Docente docenteResponsable;

    @NotNull(message = "El origen del refuerzo es obligatorio")
    @Enumerated(EnumType.STRING)
    private OrigenRefuerzo origen;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    @NotBlank(message = "La planificación es obligatoria")
    @Column(length = 1000)
    private String planificacion;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EstadoRefuerzo estado = EstadoRefuerzo.PLANIFICADO;

    private LocalDate fechaAprobacion;

    @ManyToOne
    private Usuario aprobadoPor;
}
