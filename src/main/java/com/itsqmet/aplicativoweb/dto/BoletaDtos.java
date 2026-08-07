package com.itsqmet.aplicativoweb.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * NUEVO. Art. 4.e y 37 del Acuerdo Ministerial; Cap. 8.1 del Instructivo
 * (pág. 42, Ilustración 2): distingue "Informe de progreso de aprendizaje"
 * (por periodo académico) de "Informe final anual" (todo el año, con
 * columna de supletorio si aplica y el resultado de promoción). Reemplaza
 * al "ReporteController/ReporteService" original, que importaba una clase
 * "Resumen" que tampoco existía -- ver dto/ReporteDtos.java para ese fix
 * puntual; este archivo es el que realmente construye las boletas.
 */
public final class BoletaDtos {

    private BoletaDtos() {
    }

    public record LineaMateriaDto(
            Long materiaCursoId,
            String nombreMateria,
            BigDecimal promedio,
            String equivalenciaCualitativa
    ) {}

    public record LineaDestrezaDto(
            String ambitoAprendizaje,
            String destreza,
            String escala
    ) {}

    public record LineaActividadDto(
            Long actividadId,
            String nombre,
            BigDecimal calificacion // null si el alumno todavía no tiene nota ahí
    ) {}

    // Desglose por materia con CADA nota individual del trimestre (no solo el
    // promedio) -- lo usa el PDF de Elemental/Media para mostrar la boleta
    // completa, igual que ya se ve en la pantalla de Notas.
    public record LineaMateriaDetalleDto(
            Long materiaCursoId,
            String nombreMateria,
            List<LineaActividadDto> actividades,
            BigDecimal promedio,
            String equivalenciaCualitativa
    ) {}

    public record InformeProgresoDto(
            Long alumnoId,
            String nombreCompleto,
            Long periodoAcademicoId,
            List<LineaMateriaDto> materias,
            List<LineaDestrezaDto> destrezas, // solo para Inicial/Preparatoria
            String evaluacionComportamental,
            List<LineaMateriaDetalleDto> materiasDetalle, // solo para Elemental/Media
            BigDecimal promedioFinal // promedio de "materias"; null para Inicial/Preparatoria
    ) {}

    public record LineaMateriaAnualDto(
            Long materiaCursoId,
            String nombreMateria,
            List<BigDecimal> promediosPorPeriodo,
            BigDecimal supletoria,
            BigDecimal promedioFinal
    ) {}

    public record InformeFinalAnualDto(
            Long alumnoId,
            String nombreCompleto,
            Long anioLectivoId,
            List<LineaMateriaAnualDto> materias,
            BigDecimal promedioAnualGeneral,
            List<String> evaluacionComportamentalPorPeriodo,
            String resultadoPromocion
    ) {}
}
