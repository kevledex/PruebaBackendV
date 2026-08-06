package com.itsqmet.aplicativoweb.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.itsqmet.aplicativoweb.enums.TipoAdaptacion;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

import java.util.List;

/**
 * AJUSTADO respecto del análisis MINEDUC:
 *  - "grado" y "paralelo" (String libres) se reemplazan por una relación a
 *    Curso (Art. 5, 6 y 7 del Acuerdo: la escala y las reglas de evaluación
 *    dependen del nivel/subnivel, que ahora vive en Curso). Esto además
 *    habilita la línea "alumno.setCurso(...)" que en el servicio original
 *    estaba comentada porque el campo no existía.
 *  - Se agregan los campos de necesidades educativas específicas (Cap. X,
 *    Art. 30 del Acuerdo): tieneNecesidadesEducativasEspecificas y
 *    tipoAdaptacion, usados por PromedioService (para forzar evaluación
 *    cualitativa en proyectos interdisciplinares, Cap. 2.4 pág. 23 del
 *    Instructivo) y por el proceso de repitencia excepcional (Art. 23).
 */

@Entity
@Data
@NoArgsConstructor

public class Alumno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La cédula no puede estar vacía")
    @Column(unique = true, length = 10)
    private String cedula;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 carácteres")
    private String nombres;

    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 carácteres")
    private String apellidos;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    private LocalDate fechaNacimiento;

    @NotNull(message = "El curso es obligatorio")
    @ManyToOne(optional = false)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "El representante solo debe contener letras y espacios")
    private String representante;

    @NotBlank(message = "El teléfono no puede estar vacío")
    @Pattern(regexp = "\\d{10}", message = "El teléfono debe tener exactamente 10 dígitos")
    private String telefono;

    private String estado = "Activo";

    private boolean tieneNecesidadesEducativasEspecificas;

    @Enumerated(EnumType.STRING)
    private TipoAdaptacion tipoAdaptacion; // null si no aplica

    @ManyToOne
    @JsonIgnoreProperties({"alumnos"})
    private Representante representanteRegistro;

    @OneToMany(mappedBy = "alumno")
    @JsonIgnore
    private List<Nota> notas;

}
