package com.itsqmet.aplicativoweb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"alumno_id", "actividad_id"}
        )
)


/**
 * CORREGIDO/AJUSTADO:
 *  - BUG: el @UniqueConstraint ya referenciaba "actividad_id", pero la
 *    entidad no tenía ningún campo "actividad" (solo "materia"), y
 *    NotaRepository.findByActividadId(...) derivaba una consulta sobre esa
 *    propiedad inexistente -> Spring Data fallaba al arrancar con
 *    PropertyReferenceException. Se agrega la relación real a Actividad
 *    (de ahí se obtienen tipoEvaluacion, caracter y periodoAcademico; el
 *    campo "materia" se retira por redundante, se llega a él vía
 *    actividad.getMateriaCurso().getMateria()).
 *  - "calificacion" pasa de Double a BigDecimal: el Cap. 2.3 (pág. 15) del
 *    Instructivo exige truncar a 2 decimales SIN redondear, algo propenso
 *    a errores de precisión con aritmética de punto flotante.
 *  - Se agregan "esNotaDeMejora" y "notaOriginal" (Art. 11/12 del Acuerdo):
 *    permiten conservar el historial de la calificación inicial cuando se
 *    aplica una mejora, y aplicar la regla "si el promedio de la mejora es
 *    inferior al inicial, se mantiene el inicial" (Tablas 18-22 del
 *    Instructivo).
 *  - Se agrega "calificacionSupletoriaCruda" (Art. 21 del Acuerdo, Tabla 23
 *    del Instructivo): guarda lo que el estudiante obtuvo realmente en la
 *    supletoria, mientras que "calificacion" queda topada en 7.00.
 */

public class Nota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La calificación no puede ser nula")
    @DecimalMin(value = "0.0", inclusive = true, message = "La calificación mínima es 0")
    @DecimalMax(value = "10.0", inclusive = true, message = "La calificación máxima es 10")
    @Digits(integer = 2, fraction = 2, message = "La calificación admite máximo 2 decimales")
    private BigDecimal calificacion;

    @Size(max = 500, message = "La observación no puede superar los 500 caracteres")
    private String observacion;

    @NotNull(message = "La fecha de registro no puede ser nula")
    @PastOrPresent(message = "La fecha de registro no puede ser futura")
    private LocalDate fecha;

    @ManyToOne(optional = false)
    @JoinColumn(name = "alumno_id", nullable = false)
    private  Alumno alumno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "actividad_id", nullable = false)
    @JsonIgnoreProperties({"materiaCurso"})
    private Actividad actividad;

    private boolean esNotaDeMejora;

    @ManyToOne
    @JoinColumn(name = "nota_original_id")
    private Nota notaOriginal;

    private BigDecimal calificacionSupletoriaCruda;
}
