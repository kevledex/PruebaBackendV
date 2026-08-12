package com.itsqmet.aplicativoweb.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

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
 *
 * CORREGIDO: la mitad de los manejadores devolvía "ResponseEntity<String>"
 * (cuerpo de texto plano) y la otra mitad "ResponseEntity<Map<String,
 * String>>" (JSON). El frontend (client.ts) solo sabe leer JSON: al recibir
 * texto plano, "response.json()" fallaba silenciosamente y el error real
 * del backend se perdía, mostrándose al usuario el mensaje genérico
 * "Error HTTP 409/404/400". Se unifica todo a JSON con clave "error".
 *
 * CORREGIDO: el manejador catch-all filtraba "ex.getMessage()" (detalle
 * interno de la excepción, potencialmente una traza de Hibernate/JDBC) tal
 * cual al cliente. Ahora se registra en el log del servidor y al usuario se
 * le devuelve un mensaje genérico y amigable.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    //ALUMNO
    @ExceptionHandler(AlumnoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> manejarNoEncontrado(AlumnoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    //DATOS INVALIDOS (Lo vamos a reutilizar para alumnos y materia)
    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<Map<String, String>> manejarDatosInvalidos(DatosInvalidosException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }


    //NOTA
    @ExceptionHandler(NotaNoEncontradaException.class)
    public ResponseEntity<Map<String, String>> manejarNotaNoEncontrada(NotaNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    //MATERIA
    @ExceptionHandler(MateriaNoEncontradaException.class)
    public ResponseEntity<Map<String, String>> manejarMateriaNoEncontrada(MateriaNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    //RECURSOS NUEVOS: estructura académica, alertas, refuerzos, mejoras,
    //supletorias, revisión/apelación, socioemocional, NEE, promoción, boletas
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> manejarRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    //BaseCrudController (Roles, Docentes, Evaluaciones-Destreza, etc.) lanza
    //NoSuchElementException al no encontrar un registro por id; sin este
    //manejador caía en el catch-all genérico y devolvía 500 en vez de 404.
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> manejarNoEncontrado(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    //REGLAS DE NEGOCIO DE LA NORMATIVA MINEDUC (cupos, plazos, roles protegidos, etc.)
    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<Map<String, String>> manejarOperacionNoPermitida(OperacionNoPermitidaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
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

    //VIOLACION DE RESTRICCIONES DE BASE DE DATOS (ej: cedula/usuario duplicados,
    //vincular dos veces el mismo usuario a un docente/representante)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> manejarIntegridadDatos(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "El dato ingresado ya está en uso o entra en conflicto con un registro existente."));
    }

    //RESPONSESTATUSEXCEPTION (usada directamente por AuthService/ReporteController/etc
    //para 401/403/400 puntuales): sin este manejador, al no ser más específico que
    //"Exception", caía en el catch-all genérico y siempre devolvía 500 ignorando el
    //código HTTP real que la excepción ya traía.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> manejarResponseStatusException(ResponseStatusException ex) {
        String mensaje = ex.getReason() != null ? ex.getReason() : "Error en la solicitud";
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", mensaje));
    }

    //JSON MAL FORMADO O CON TIPOS INCOMPATIBLES
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> manejarJsonInvalido(HttpMessageNotReadableException ex) {
        String causa = ex.getMostSpecificCause().getMessage();
        String mensaje = "Los datos enviados no tienen un formato válido."
                + (causa != null ? " (" + causa + ")" : "");
        return ResponseEntity.badRequest().body(Map.of("error", mensaje));
    }

    //ERRORES GENERALES: no se filtra "ex.getMessage()" al cliente porque puede
    //contener detalle interno (SQL, stacktrace de una librería, etc.); se
    //registra en el log del servidor para diagnóstico y se responde un mensaje
    //genérico y amigable.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> manejarErroresGenerales(Exception ex) {
        log.error("Error inesperado no controlado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Ocurrió un problema en el servidor. Intenta nuevamente en unos minutos."));
    }

}
