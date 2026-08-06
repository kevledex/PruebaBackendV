package com.itsqmet.aplicativoweb.enums;

/**
 * Estados del flujo de revisión y apelación de calificaciones (Art. 40 del
 * RGLOEI; Cap. 7 del Instructivo, pág. 37-40). Contempla las dos instancias:
 * revisión institucional (comisión de rectificación) y apelación distrital
 * (comisión de apelación, resolución definitiva).
 */
public enum EstadoSolicitud {
    PENDIENTE,
    EN_REVISION_INSTITUCIONAL,
    RESUELTA_RATIFICADA,
    RESUELTA_RECTIFICADA,
    NUEVA_EVALUACION_REQUERIDA,
    EN_APELACION_DISTRITAL,
    RESUELTA_DEFINITIVA
}
