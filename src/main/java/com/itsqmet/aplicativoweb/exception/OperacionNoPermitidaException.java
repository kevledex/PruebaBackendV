package com.itsqmet.aplicativoweb.exception;

/**
 * NUEVO: excepción genérica (409 Conflict) para violaciones de reglas de
 * negocio de la normativa MINEDUC: cupos de mejora de calificación agotados,
 * roles protegidos, plazos de revisión/apelación vencidos, refuerzo
 * pedagógico no finalizado antes de una supletoria, etc.
 */
public class OperacionNoPermitidaException extends RuntimeException {

    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
