package com.itsqmet.aplicativoweb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.itsqmet.aplicativoweb.enums.NivelEducativo;
import com.itsqmet.aplicativoweb.enums.Sostenimiento;
import com.itsqmet.aplicativoweb.enums.TipoOrganizacionPeriodo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * NUEVO. Reemplaza los campos "grado" y "paralelo" (String libres) que
 * tenía Alumno. Es la base de la que dependen todas las reglas de
 * evaluación aplicables (escala cualitativa/cuantitativa según
 * NivelEducativo, organización trimestral/quimestral, etc.) según Art. 5,
 * 6 y 7 del Acuerdo Ministerial. SecurityConfig ya protegía la ruta
 * "/api/cursos/**" para ADMIN, pero la entidad y el controlador no
 * existían.
 */
@Entity
@Data
@NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"grado", "paralelo", "anio_lectivo_id"}))
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El nivel educativo es obligatorio")
    @Enumerated(EnumType.STRING)
    private NivelEducativo nivel;

    @NotBlank(message = "El grado/curso no puede estar vacío")
    private String grado; // "Cuarto", "Quinto", "Sexto", "Séptimo"... (la institución solo llega hasta 7mo EGB)

    @NotBlank(message = "El paralelo no puede estar vacío")
    @Pattern(regexp = "[A-Z]", message = "El paralelo debe ser una letra mayúscula (A, B, C...)")
    private String paralelo;

    @NotNull(message = "El sostenimiento es obligatorio")
    @Enumerated(EnumType.STRING)
    private Sostenimiento sostenimiento;

    @NotNull(message = "El tipo de organización del periodo académico es obligatorio")
    @Enumerated(EnumType.STRING)
    private TipoOrganizacionPeriodo tipoOrganizacion;

    @NotNull(message = "El año lectivo es obligatorio")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "anio_lectivo_id", nullable = false)
    private AnioLectivo anioLectivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    @JsonIgnoreProperties({"rol"})
    private Docente tutor;
}
