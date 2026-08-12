package com.itsqmet.aplicativoweb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * NUEVO. Es la "oferta" real de una Materia dentro de un Curso concreto: qué
 * docente la dicta y cuántos periodos pedagógicos semanales tiene. Esto es
 * necesario porque:
 *  - La Tabla 35 del Instructivo (pág. 47) define el número mínimo de
 *    aportes en función de los periodos pedagógicos semanales, un dato que
 *    depende de la oferta concreta (curso+materia), no de la materia como
 *    catálogo aislado.
 *  - La comisión de rectificación de calificaciones (Cap. 7 del Instructivo)
 *    exige poder identificar qué docente calificó, para excluirlo de la
 *    comisión.
 * Actividad y Nota deben referenciar MateriaCurso en vez de Materia
 * directamente (ver paquete de ajustes a Actividad/Nota).
 */
@Entity
@Data
@NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"materia_id", "curso_id"}))
public class MateriaCurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La materia es obligatoria")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;

    @NotNull(message = "El curso es obligatorio")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"rol"})
    private Docente docente; // nullable: se asigna luego vía DocenteService.asignarMaterias

    @Min(value = 1, message = "Debe tener al menos 1 periodo pedagógico semanal")
    @Max(value = 20, message = "No puede tener más de 20 periodos pedagógicos semanales")
    private int periodosPedagogicosSemana;
}
