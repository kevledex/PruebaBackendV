package com.itsqmet.aplicativoweb.model;

import com.itsqmet.aplicativoweb.enums.EntidadEvaluadora;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * NUEVO. Art. 3.e y Art. 29 del Acuerdo Ministerial: proceso dinámico de
 * recolección, análisis e interpretación de datos de un estudiante con
 * necesidades educativas específicas asociadas o no a la discapacidad,
 * aplicado por equipos UDAI/DECE o, a solicitud del representante, por
 * centros privados o profesionales particulares, con manejo confidencial de
 * los datos. Es insumo (nunca la única causa) de la repitencia excepcional
 * en Preparatoria/Elemental (Art. 23) y de la alerta temprana (Art. 9).
 */
@Entity
@Data
@NoArgsConstructor
public class EvaluacionPsicopedagogica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El alumno es obligatorio")
    @ManyToOne(optional = false)
    private Alumno alumno;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotNull(message = "La entidad evaluadora es obligatoria")
    @Enumerated(EnumType.STRING)
    private EntidadEvaluadora entidadEvaluadora;

    private String profesionalResponsable;

    @NotBlank(message = "Los resultados son obligatorios")
    @Column(length = 3000)
    private String resultados;

    @Column(length = 2000)
    private String recomendaciones;

    /**
     * Marca de refuerzo: el acceso de lectura a esta entidad debe
     * restringirse a ADMIN, DECE y el/los docentes asignados al alumno (ver
     * EvaluacionPsicopedagogicaService); nunca debe exponerse en el
     * endpoint general de Alumno.
     */
    private boolean confidencial = true;
}
