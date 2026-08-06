package com.itsqmet.aplicativoweb.exception;

public class MateriaNoEncontradaException extends RuntimeException {
    public MateriaNoEncontradaException(Long id) {
        super ("Materia con id" + id + " no existe");
    }
}
