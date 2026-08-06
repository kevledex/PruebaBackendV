package com.itsqmet.aplicativoweb.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * NUEVO. Representa el año/ciclo lectivo (Art. 4.a del Acuerdo Ministerial:
 * "Organización del año/ciclo lectivo"). Es el ancla temporal para acotar
 * los topes de mejora de calificaciones ("dentro del mismo año lectivo",
 * Art. 11 y 12) y para separar históricamente los cursos y periodos
 * académicos de cada año.
 *
 * AJUSTADO: se eliminó el campo "regimen" (y el enum RegimenEscolar) — esta
 * institución opera siempre bajo el régimen Sierra-Amazonía y nunca cambia,
 * así que no aporta información que deba modelarse.
 */
@Entity
@Data
@NoArgsConstructor
public class AnioLectivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del año lectivo no puede estar vacío")
    @Column(unique = true)
    private String nombre; // p.ej. "2024-2025"

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    /**
     * Solo debe existir un AnioLectivo con activo=true a la vez;
     * AnioLectivoService.activar(id) se encarga de esa invariante.
     */
    private boolean activo;
}
