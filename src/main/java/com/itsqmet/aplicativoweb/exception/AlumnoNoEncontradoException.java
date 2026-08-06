package com.itsqmet.aplicativoweb.exception;

public class AlumnoNoEncontradoException extends RuntimeException {
    public AlumnoNoEncontradoException(Long id){
        super( " Alumno con id" + id + " no existe");
    }
}
