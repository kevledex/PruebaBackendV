package com.itsqmet.aplicativoweb.enums;

/**
 * Estado de una planificación de refuerzo pedagógico. Debe pasar por
 * APROBADO (autoridad institucional) antes de poder ejecutarse, y por
 * FINALIZADO antes de habilitar una evaluación de mejora o supletoria
 * asociada (pág. 36 del Instructivo: "los docentes planificarán el refuerzo
 * pedagógico y lo presentarán a la autoridad para su aprobación previo a su
 * ejecución").
 */
public enum EstadoRefuerzo {
    PLANIFICADO,
    APROBADO,
    EN_EJECUCION,
    FINALIZADO
}
