package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.enums.EscalaCualitativa;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * NUEVO. Calcula automáticamente la equivalencia cualitativa (A+...E-) a
 * partir de una calificación cuantitativa, tal como exigen el Art. 6 y 7
 * del Acuerdo Ministerial ("el aplicativo, por defecto, calculará su
 * equivalencia cualitativa", pág. 10 del Instructivo) y según la Tabla 7
 * del Instructivo.
 *
 * Importante: a diferencia del promedio numérico (que se TRUNCA, sin
 * redondear, según pág. 15 del Instructivo), la equivalencia cualitativa sí
 * se obtiene REDONDEANDO el promedio ya truncado -- así lo muestra el
 * propio ejemplo de la Tabla 6 del Instructivo: "8.13≈8.00 = B+".
 */
@Service
public class EscalaCualitativaMapper {

    public EscalaCualitativa desdeNumero(BigDecimal calificacion) {
        if (calificacion == null) {
            return EscalaCualitativa.NE;
        }
        int valorRedondeado = calificacion.setScale(0, RoundingMode.HALF_UP).intValue();
        return switch (valorRedondeado) {
            case 10 -> EscalaCualitativa.A_MAS;
            case 9 -> EscalaCualitativa.A_MENOS;
            case 8 -> EscalaCualitativa.B_MAS;
            case 7 -> EscalaCualitativa.B_MENOS;
            case 6 -> EscalaCualitativa.C_MAS;
            case 5 -> EscalaCualitativa.C_MENOS;
            case 4 -> EscalaCualitativa.D_MAS;
            case 3 -> EscalaCualitativa.D_MENOS;
            case 2 -> EscalaCualitativa.E_MAS;
            default -> EscalaCualitativa.E_MENOS; // 1 o menos
        };
    }
}
