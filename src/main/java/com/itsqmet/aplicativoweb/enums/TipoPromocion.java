package com.itsqmet.aplicativoweb.enums;

/**
 * Resultado del proceso de promoción de un estudiante (Cap. VI y VIII del
 * Acuerdo Ministerial; Cap. 8 del Instructivo).
 *
 * AJUSTADO: esta institución solo llega hasta 7mo de EGB (Subnivel Media),
 * no existe Superior ni Bachillerato, así que DIRECTA/TRAS_SUPLETORIA/
 * EXCEPCIONAL_CON_CONDICIONES aplican únicamente al Subnivel Media.
 *
 *  - AUTOMATICA: Inicial, Preparatoria y Elemental (Art. 19).
 *  - DIRECTA: Subnivel Media con promedio final >= 7.00 (Art. 20).
 *  - TRAS_SUPLETORIA: promedio final entre 7.00 y 10.00 luego de supletoria (Art. 21).
 *  - EXCEPCIONAL_CON_CONDICIONES: Subnivel Media, con refuerzo pedagógico
 *    y acuerdo consensuado con representante en vez de repetir (Art. 24-25).
 *  - REPITE: repitencia directa o tras supletoria (Art. 24, 25) o repitencia
 *    excepcional en Preparatoria/Elemental (Art. 23).
 */
public enum TipoPromocion {
    AUTOMATICA,
    DIRECTA,
    TRAS_SUPLETORIA,
    EXCEPCIONAL_CON_CONDICIONES,
    REPITE,
    PENDIENTE
}
