package com.itsqmet.aplicativoweb.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * NUEVO. Catálogo de las 9 habilidades priorizadas para el Acompañamiento
 * Socioemocional en el Sistema Educativo Nacional (Cap. 5 del Instructivo,
 * pág. 31; Anexo 2: "Lineamientos para el periodo pedagógico de Cívica y
 * Acompañamiento Integral en el Aula").
 */
@Entity
@Data
@NoArgsConstructor
public class HabilidadSocioemocional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;

    @NotBlank(message = "El tipo es obligatorio")
    @Pattern(regexp = "COGNITIVA|SOCIAL|EMOCIONAL", message = "El tipo debe ser COGNITIVA, SOCIAL o EMOCIONAL")
    private String tipo;
}
