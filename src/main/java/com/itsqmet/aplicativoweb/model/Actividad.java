package com.itsqmet.aplicativoweb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.itsqmet.aplicativoweb.enums.CaracterEvaluacion;
import com.itsqmet.aplicativoweb.enums.CategoriaInsumoMedia;
import com.itsqmet.aplicativoweb.enums.TipoEvaluacion;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

/**
 * AJUSTADO respecto del análisis MINEDUC:
 *  - "tipo" (String libre) se reemplaza por "tipoEvaluacion"
 *    (TipoEvaluacion: DIAGNOSTICA/FORMATIVA/SUMATIVA -- Art. 3.f del
 *    Acuerdo), del que depende la ponderación 70/30 (Cap. 2.3 del
 *    Instructivo).
 *  - Se agrega "caracter" (CaracterEvaluacion) para distinguir evaluaciones
 *    ANTICIPADA/ATRASADA/MEJORA_DIRECTA/MEJORA_CON_REFUERZO/SUPLETORIA de
 *    una evaluación REGULAR (Art. 10, 11, 12, 16, 17, 21, 22 del Acuerdo).
 *  - Se agrega "esProyectoInterdisciplinar" (Cap. 2.3-2.4 del Instructivo):
 *    la evaluación sumativa (30%) se compone del proyecto interdisciplinar
 *    promediado con otras sumativas.
 *  - "periodo" (String validado con regex "Q1|Q2|Quimestre 1|Quimestre 2",
 *    que además asumía organización quimestral por defecto, contradiciendo
 *    que el sostenimiento fiscal es TRIMESTRAL) se reemplaza por la
 *    relación real a PeriodoAcademico.
 *  - "materia" se reemplaza por "materiaCurso" (la oferta real
 *    materia+curso+docente, ver paquete de estructura académica).
 *  - Se retira "@FutureOrPresent" de "fecha": impedía registrar evaluaciones
 *    atrasadas (Art. 17 del Acuerdo permite aplicarlas hasta 5/15 días
 *    después de su fecha original, es decir con fecha ya pasada). La
 *    ventana de tiempo válida ahora se valida en ActividadService según el
 *    "caracter" de la evaluación.
 */
@Entity
@Data
@NoArgsConstructor

public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @NotBlank(message = "El nombre de la actividad no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private  String nombre;

    @NotNull(message = "El tipo de evaluación es obligatorio")
    @Enumerated(EnumType.STRING)
    private TipoEvaluacion tipoEvaluacion;

    @NotNull(message = "El carácter de la evaluación es obligatorio")
    @Enumerated(EnumType.STRING)
    private CaracterEvaluacion caracter = CaracterEvaluacion.REGULAR;

    private boolean esProyectoInterdisciplinar;

    @NotNull(message = "El periodo académico es obligatorio")
    @ManyToOne(optional = false)
    private PeriodoAcademico periodoAcademico;

    @NotNull(message = "La fecha de la actividad es obligatoria")
    private LocalDate fecha;

    @ManyToOne(optional = false)
    @JoinColumn(name = "materia_curso_id", nullable = false)
    @JsonIgnoreProperties({"docente"})
    private MateriaCurso materiaCurso;

    /**
     * NUEVO: solo aplica a cursos de EGB Media. Determina en qué
     * sub-categoría del cálculo jerárquico 70/15/15 entra esta actividad
     * (Tareas/Individuales/Lecciones/Grupales alimentan el 70% formativo;
     * Proyecto y Examen son el 15%+15% restante). Nulo para Inicial,
     * Preparatoria y Elemental, que no usan esta sub-categorización.
     */
    @Enumerated(EnumType.STRING)
    private CategoriaInsumoMedia categoriaInsumoMedia;

}
