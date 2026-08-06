package com.itsqmet.aplicativoweb.enums;

/**
 * NUEVO. Sub-categoría de una Actividad de tipo FORMATIVA (o del insumo
 * sumativo) dentro de EGB Media (5to a 7mo EGB), replicando cómo lo llevan
 * las planillas reales de la institución: las evaluaciones formativas se
 * agrupan en cuatro sub-categorías (Tareas, Individuales, Lecciones,
 * Grupales) que alimentan el cálculo jerárquico de Insumo 1/Insumo 2, y el
 * insumo sumativo se divide en Proyecto Interdisciplinar (15%) y Examen
 * Trimestral (15%). Solo aplica a cursos de nivel MEDIA -- Inicial,
 * Preparatoria y Elemental no usan esta categorización.
 */
public enum CategoriaInsumoMedia {
    TAREA,
    INDIVIDUAL,
    LECCION,
    GRUPAL,
    PROYECTO,
    EXAMEN
}
