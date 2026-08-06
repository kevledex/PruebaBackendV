package com.itsqmet.aplicativoweb.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * CORREGIDO: a esta clase le faltaba la anotación "@RestControllerAdvice"
 * (o "@ControllerAdvice"), por lo que Spring nunca la registraba como
 * manejador global -- todos los "@ExceptionHandler" definidos aquí eran
 * código muerto y cualquier excepción de negocio se devolvía como un 500
 * genérico en vez del código HTTP correcto. Se agrega la anotación y se
 * suman los manejadores para las nuevas excepciones genéricas
 * (RecursoNoEncontradoException, OperacionNoPermitidaException) usadas por
 * todos los servicios nuevos.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    //ALUMNO
    @ExceptionHandler(AlumnoNoEncontradoException.class)
    public ResponseEntity<String> manejarNoEncontrado(AlumnoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    //DATOS INVALIDOS (Lo vamos a reutilizar para alumnos y materia)
    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<String> manejarDatosInvalidos(DatosInvalidosException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }


    //NOTA
    @ExceptionHandler(NotaNoEncontradaException.class)
    public ResponseEntity<String> manejarNotaNoEncontrada(NotaNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    //MATERIA
    @ExceptionHandler(MateriaNoEncontradaException.class)
    public ResponseEntity<String> manejarMateriaNoEncontrada(MateriaNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    //RECURSOS NUEVOS: estructura académica, alertas, refuerzos, mejoras,
    //supletorias, revisión/apelación, socioemocional, NEE, promoción, boletas
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<String> manejarRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    //BaseCrudController (Roles, Docentes, Evaluaciones-Destreza, etc.) lanza
    //NoSuchElementException al no encontrar un registro por id; sin este
    //manejador caía en el catch-all genérico y devolvía 500 en vez de 404.
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> manejarNoEncontrado(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    //REGLAS DE NEGOCIO DE LA NORMATIVA MINEDUC (cupos, plazos, roles protegidos, etc.)
    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<String> manejarOperacionNoPermitida(OperacionNoPermitidaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    //VALIDACIÓN (Bean Validation vía @Valid en el controlador)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarArgumentoInvalido(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Map.of("error", mensaje.isBlank() ? "Datos inválidos" : mensaje));
    }

    //VALIDACIÓN (Bean Validation automática de JPA/Hibernate al guardar, sin pasar por @Valid)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> manejarViolacionRestriccion(ConstraintViolationException ex) {
        String mensaje = ex.getConstraintViolations().stream()
                .map(violacion -> violacion.getPropertyPath() + ": " + violacion.getMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Map.of("error", mensaje.isBlank() ? "Datos inválidos" : mensaje));
    }

    //JSON MAL FORMADO O CON TIPOS INCOMPATIBLES
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> manejarJsonInvalido(HttpMessageNotReadableException ex) {
        String causa = ex.getMostSpecificCause().getMessage();
        String mensaje = "Los datos enviados no tienen un formato válido."
                + (causa != null ? " (" + causa + ")" : "");
        return ResponseEntity.badRequest().body(Map.of("error", mensaje));
    }

    //ERRORES GENERALES
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> manejarErroresGenerales(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error inesperado: " + ex.getMessage());
    }

}
