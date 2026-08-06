package com.itsqmet.aplicativoweb.model;

import com.itsqmet.aplicativoweb.enums.EstadoSolicitud;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * NUEVO. Art. 40 del RGLOEI; Cap. 7 del Instructivo (pág. 37-40): derecho de
 * revisión y apelación de calificaciones. Modela las dos instancias:
 * revisión institucional (comisión de rectificación) y apelación distrital
 * (comisión de apelación, resolución definitiva sin nueva instancia).
 */
@Entity
@Data
@NoArgsConstructor
public class SolicitudRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La nota cuestionada es obligatoria")
    @ManyToOne(optional = false)
    private Nota notaCuestionada;

    @NotNull(message = "El solicitante es obligatorio")
    @ManyToOne(optional = false)
    private Representante solicitante;

    @NotNull(message = "La fecha de solicitud es obligatoria")
    private LocalDate fechaSolicitud;

    @NotBlank(message = "El motivo es obligatorio")
    @Column(length = 1000)
    private String motivo;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    // Vicerrector + coordinador(es) de área + un docente por área; nunca debe
    // incluir al docente que otorgó la calificación original (Cap. 7, pág. 37).
    @ManyToMany
    private List<Docente> comisionRectificacion;

    private LocalDate fechaLimiteResolucion; // fechaSolicitud + 3 días laborables

    @Column(length = 2000)
    private String informeResultado;

    private boolean apelada;
    private LocalDate fechaApelacion;

    @ManyToMany
    private List<Docente> comisionApelacion;

    @Column(length = 2000)
    private String resolucionApelacion;
}
