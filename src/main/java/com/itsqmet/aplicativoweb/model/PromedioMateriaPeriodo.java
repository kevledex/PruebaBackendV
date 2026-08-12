package com.itsqmet.aplicativoweb.model;

import com.itsqmet.aplicativoweb.enums.EscalaCualitativa;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * NUEVO. Tabla derivada/cacheada con el resultado del cálculo de
 * PromedioService, para no tener que recalcular sobre la marcha cada vez
 * que se genera una boleta (ver paquete de boletas). Se recalcula al cerrar
 * un PeriodoAcademico o mediante el endpoint de recálculo manual.
 */
@Entity
@Data
@NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"alumno_id", "materia_curso_id", "periodo_academico_id"}))
public class PromedioMateriaPeriodo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "materia_curso_id", nullable = false)
    private MateriaCurso materiaCurso;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "periodo_academico_id", nullable = false)
    private PeriodoAcademico periodoAcademico;

    private BigDecimal promedioFormativa; // Subnivel Media: EVALUACION_FORMATIVA_TOTAL (Nivel 2). Null para Elemental.
    private BigDecimal promedioSumativa;  // sin uso desde el cálculo jerárquico 70/15/15; se conserva por compatibilidad
    private BigDecimal promedioFinal;     // PROMEDIO_TRIMESTRAL, truncado a 2 decimales, nunca redondeado

    // --- Desglose del cálculo jerárquico de EGB Media (70% formativo + 15% proyecto + 15% examen) ---
    // Nivel 1: promedio simple truncado de cada sub-categoría de actividades formativas.
    private BigDecimal promedioTareas;
    private BigDecimal promedioIndividuales;
    private BigDecimal promedioLecciones;
    private BigDecimal promedioGrupales;

    // Nivel 2: INSUMO_1 = (Prom_Tareas + Prom_Individuales + Prom_Lecciones) / 3 ; INSUMO_2 = Prom_Grupales
    private BigDecimal insumo1;
    private BigDecimal insumo2;

    // Insumo sumativo: nota de Proyecto Interdisciplinar y de Examen Trimestral (cada uno pesa 15%)
    private BigDecimal notaProyecto;
    private BigDecimal notaExamen;

    // Nivel 3: ponderación final -- P1 = Formativa*0.70, P2 = Proyecto*0.15, P3 = Examen*0.15
    private BigDecimal p1Formativo;
    private BigDecimal p2Proyecto;
    private BigDecimal p3Examen;

    @Enumerated(EnumType.STRING)
    private EscalaCualitativa equivalenciaCualitativa;
}
