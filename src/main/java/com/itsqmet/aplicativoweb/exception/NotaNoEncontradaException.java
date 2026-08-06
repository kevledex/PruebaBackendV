package com.itsqmet.aplicativoweb.exception;

public class NotaNoEncontradaException extends RuntimeException {
    public NotaNoEncontradaException(Long id) {
        super("Nota con id" + id + "No existe");
    }
}
