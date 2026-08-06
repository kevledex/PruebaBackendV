package com.itsqmet.aplicativoweb.model;

import com.itsqmet.aplicativoweb.enums.EscalaDestrezaInicial;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * NUEVO. En el Nivel de Educación Inicial y el Subnivel de Preparatoria la
 * evaluación es exclusivamente cualitativa, por destreza/ámbito de
 * aprendizaje, y el Instructivo es explícito: "no se debe realizar un
 * promedio al momento de calificar" (Cap. 2.1, pág. 6; Tablas 1 a 4). La
 * entidad Nota (cuantitativa, con promedio) no encaja para este nivel/
 * subnivel, por lo que se modela por separado.
 */
@Entity
@Data
@NoArgsConstructor
public class EvaluacionDestreza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El alumno es obligatorio")
    @ManyToOne(optional = false)
    private Alumno alumno;

    @NotBlank(message = "El ámbito de aprendizaje es obligatorio")
    private String ambitoAprendizaje; // "Identidad y Autonomía", "Convivencia"...

    @NotBlank(message = "La destreza es obligatoria")
    private String destreza; // código + descripción del Currículo Nacional

    @NotNull(message = "El periodo académico es obligatorio")
    @ManyToOne(optional = false)
    private PeriodoAcademico periodoAcademico;

    /**
     * CORREGIDO: usaba "EscalaCualitativa" (la escala de letras A+/A-/B+/B-
     * de EGB Elemental/Media), que no es la escala que exige el Instructivo
     * para Inicial/Preparatoria. Se usa la escala correcta de 4 valores
     * (Alcanzada/En proceso/Iniciada/No evaluada).
     */
    @NotNull(message = "La escala es obligatoria")
    @Enumerated(EnumType.STRING)
    private EscalaDestrezaInicial escala;
}
