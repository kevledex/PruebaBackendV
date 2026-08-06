package com.itsqmet.aplicativoweb.enums;

/**
 * NUEVO. Escala cualitativa exclusiva de Educación Inicial y el subnivel de
 * Preparatoria (Cap. 2.1 del Instructivo, Tablas 1 a 4): estos niveles NO
 * usan la escala de letras A+/A-/B+/B-... (esa es la escala de calificación
 * de EGB Elemental/Media, ver EscalaCualitativa), sino una escala de cuatro
 * valores sobre el nivel de desarrollo de cada destreza.
 */
public enum EscalaDestrezaInicial {
    ALCANZADA("A", "Destreza alcanzada"),
    EN_PROCESO("EP", "Destreza en proceso"),
    INICIADA("I", "Destreza iniciada"),
    NO_EVALUADA("NE", "No evaluada en el periodo académico");

    private final String codigo;
    private final String descripcion;

    EscalaDestrezaInicial(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
