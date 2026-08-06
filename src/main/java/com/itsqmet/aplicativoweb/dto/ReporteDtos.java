package com.itsqmet.aplicativoweb.dto;

/**
 * NUEVO: ReporteController importaba
 * "com.itsqmet.aplicativoweb.dto.ReporteDtos.Resumen", pero la clase no
 * existía en el proyecto (el controlador no compilaba). Se define el
 * resumen de indicadores para el dashboard.
 */
public final class ReporteDtos {

    private ReporteDtos() {
    }

    public record Resumen(
            long totalAlumnos,
            long totalDocentes,
            long totalMaterias,
            long totalCursos,
            long alertasBajoRendimientoActivas,
            long solicitudesRevisionPendientes,
            String usuarioAutenticado
    ) {}
}
