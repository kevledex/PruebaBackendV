package com.itsqmet.aplicativoweb.enums;

/**
 * Carácter especial de una evaluación dentro del periodo académico. Una
 * evaluación REGULAR es la que se rinde en su fecha normal de cronograma;
 * las demás tienen ventanas de tiempo y topes propios definidos en la
 * normativa:
 *  - ANTICIPADA: Art. 16 del Acuerdo (solicitud 15 días antes; 30 días para finales).
 *  - ATRASADA: Art. 17 del Acuerdo (máx. 5 días tras el retorno; 15 días para finales).
 *  - MEJORA_DIRECTA: Art. 10 y 11 del Acuerdo (calificación entre 7.00 y 9.00).
 *  - MEJORA_CON_REFUERZO: Art. 10 y 12 del Acuerdo (calificación entre 0.01 y 6.99).
 *  - SUPLETORIA: Art. 21 y 22 del Acuerdo (promedio final entre 4.01 y 6.99).
 */
public enum CaracterEvaluacion {
    REGULAR,
    ANTICIPADA,
    ATRASADA,
    MEJORA_DIRECTA,
    MEJORA_CON_REFUERZO,
    SUPLETORIA
}
