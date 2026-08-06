package com.itsqmet.aplicativoweb.exception;

public class DatosInvalidosException extends RuntimeException {
    public DatosInvalidosException(String mensaje){
        super(mensaje);
    }
}
