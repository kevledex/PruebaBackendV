package com.itsqmet.aplicativoweb.model;

import com.itsqmet.aplicativoweb.enums.TipoPromocion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * NUEVO. Cap. VI y VIII del Acuerdo Ministerial; Cap. 8 del Instructivo
 * (pág. 41-42). Registra el resultado de promoción de un alumno en un año
 * lectivo, incluyendo la documentación obligatoria para la "promoción
 * excepcional con condiciones" del Subnivel Media (plan de refuerzo,
 * acuerdo consensuado firmado, informe de Junta de Docentes) y para la
 * repitencia excepcional en Preparatoria/Elemental (evaluación
 * psicopedagógica, solicitud del representante). Sin esta documentación
 * completa, el Instructivo (pág. 42) exige que el estudiante repita.
 */
@Entity
@Data
@NoArgsConstructor
public class RegistroPromocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El alumno es obligatorio")
    @ManyToOne(optional = false)
    private Alumno alumno;

    @NotNull(message = "El año lectivo es obligatorio")
    @ManyToOne(optional = false)
    private AnioLectivo anioLectivo;

    @NotNull(message = "El curso de origen es obligatorio")
    @ManyToOne(optional = false)
    private Curso cursoOrigen;

    @ManyToOne
    private Curso cursoDestino; // null mientras esté PENDIENTE o si el resultado es REPITE

    @NotNull
    @Enumerated(EnumType.STRING)
    private TipoPromocion tipoPromocion = TipoPromocion.PENDIENTE;

    private LocalDate fechaResolucion;

    // Documentación obligatoria para EXCEPCIONAL_CON_CONDICIONES (Subnivel Media)
    @ManyToOne
    private RefuerzoPedagogico planRefuerzo;
    private boolean acuerdoConsensuadoFirmado;
    private LocalDate fechaAcuerdoConsensuado;
    @Column(length = 2000)
    private String informeJuntaDocentes;

    // Documentación para repitencia excepcional (Preparatoria/Elemental)
    @ManyToOne
    private EvaluacionPsicopedagogica evaluacionPsicopedagogicaSoporte;
    private boolean solicitudRepresentanteRecibida;
    private boolean yaAplicoRepitenciaExcepcionalAntes; // valida la regla "por única vez" del Art. 23

    @Column(length = 1500)
    private String observaciones;
}
