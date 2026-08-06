package com.itsqmet.aplicativoweb.exception;

/**
 * NUEVO: excepción genérica de "no encontrado" (404) para todas las
 * entidades nuevas del backend (estructura académica, alertas, refuerzos,
 * mejoras, supletorias, revisión/apelación, socioemocional, NEE, promoción,
 * boletas), evitando crear una clase de excepción casi idéntica por cada
 * entidad como ocurría con AlumnoNoEncontradoException/
 * MateriaNoEncontradaException/NotaNoEncontradaException.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public RecursoNoEncontradoException(String recurso, Long id) {
        super(recurso + " con id " + id + " no existe");
    }
}
