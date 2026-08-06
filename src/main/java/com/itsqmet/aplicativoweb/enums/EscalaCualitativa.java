package com.itsqmet.aplicativoweb.enums;

import java.math.BigDecimal;

/**
 * Escala cualitativa oficial del RGLOEI (Tablas 1, 2, 7, 8 y 11 del
 * Instructivo de Evaluación Estudiantil Sierra-Amazonía 2024-2025).
 * Cada nivel tiene una equivalencia numérica exacta (10=A+ ... 1=E-) que el
 * sistema debe calcular automáticamente a partir de la calificación
 * cuantitativa (Art. 6 y 7 del Acuerdo Ministerial), nunca ingresarse a mano.
 * NE se usa para destrezas no abordadas/evaluadas en el periodo (Tabla 2/3/4).
 *
 * AJUSTADO: la Tabla 11 del Instructivo agrupa esta escala en "Alcanza los
 * aprendizajes / Está próximo a alcanzar / No alcanza los aprendizajes" para
 * los Subniveles Media, Superior y Bachillerato. Como esta institución solo
 * llega hasta 7mo EGB (Subnivel Media), el campo se renombra de
 * "categoriaMediaBachillerato" a "categoriaSubnivelMedia".
 */

public enum EscalaCualitativa {
    A_MAS(new BigDecimal("10"), "A+", "Destreza o aprendizaje alcanzado", "Alcanza los aprendizajes"),
    A_MENOS(new BigDecimal("9"), "A-", "Destreza o aprendizaje alcanzado", "Alcanza los aprendizajes"),
    B_MAS(new BigDecimal("8"), "B+", "Destreza o aprendizaje en proceso de desarrollo", "Alcanza los aprendizajes"),
    B_MENOS(new BigDecimal("7"), "B-", "Destreza o aprendizaje en proceso de desarrollo", "Alcanza los aprendizajes"),
    C_MAS(new BigDecimal("6"), "C+", "Destreza o aprendizaje en proceso de desarrollo", "Está próximo a alcanzar"),
    C_MENOS(new BigDecimal("5"), "C-", "Destreza o aprendizaje en proceso de desarrollo", "Está próximo a alcanzar"),
    D_MAS(new BigDecimal("4"), "D+", "Destreza o aprendizaje iniciado", "No alcanza los aprendizajes"),
    D_MENOS(new BigDecimal("3"), "D-", "Destreza o aprendizaje iniciado", "No alcanza los aprendizajes"),
    E_MAS(new BigDecimal("2"), "E+", "Destreza o aprendizaje iniciado", "No alcanza los aprendizajes"),
    E_MENOS(new BigDecimal("1"), "E-", "Destreza o aprendizaje iniciado", "No alcanza los aprendizajes"),
    NE(null, "NE", "No evaluado en el periodo académico", "No evaluado");

    private final BigDecimal equivalenteNumerico;
    private final String codigo;
    private final String descripcionRGLOEI;
    private final String categoriaSubnivelMedia;

    EscalaCualitativa(BigDecimal equivalenteNumerico, String codigo,
                       String descripcionRGLOEI, String categoriaSubnivelMedia) {
        this.equivalenteNumerico = equivalenteNumerico;
        this.codigo = codigo;
        this.descripcionRGLOEI = descripcionRGLOEI;
        this.categoriaSubnivelMedia = categoriaSubnivelMedia;
    }

    public BigDecimal getEquivalenteNumerico() {
        return equivalenteNumerico;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcionRGLOEI() {
        return descripcionRGLOEI;
    }

    public String getCategoriaSubnivelMedia() {
        return categoriaSubnivelMedia;
    }
}
