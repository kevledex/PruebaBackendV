package com.itsqmet.aplicativoweb.enums;

/**
 * Nivel/subnivel educativo del Sistema Nacional de Educación (Art. 5, 6, 7 del
 * Acuerdo Ministerial MINEDUC-MINEDUC-2024-00031-A). Determina la escala de
 * evaluación aplicable (cualitativa pura, cuantitativa con equivalencia, o
 * cuantitativa 70/30) y las reglas de promoción/repitencia correspondientes.
 *
 * AJUSTADO: esta institución solo ofrece hasta 7mo de Educación General
 * Básica (Subnivel Elemental y Subnivel Media), por lo que se eliminan
 * SUPERIOR, BACHILLERATO_GENERAL y BACHILLERATO_TECNICO. No crear cursos,
 * materias ni reglas que dependan de esos niveles.
 */
public enum NivelEducativo {
    INICIAL,
    PREPARATORIA,
    ELEMENTAL,
    MEDIA
}
