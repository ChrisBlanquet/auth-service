package com.ayuntamiento.auth.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorDetalles> handleResourceAlreadyExists(
            ResourceAlreadyExistsException ex, 
            WebRequest request) {
        
        ErrorDetalles error = new ErrorDetalles(
            LocalDateTime.now(),
            ex.getMessage(),
            request.getDescription(false),
            "CONFLICTO_DATOS_DUPLICADOS"
        );
        
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetalles> ResourceNoExistsExeption(
    		ResourceNotFoundException ex, 
            WebRequest request) {
        
        ErrorDetalles error = new ErrorDetalles(
            LocalDateTime.now(),
            ex.getMessage(),
            request.getDescription(false),
            "RECURSO_NO_ENCONTRADO"
        );
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
 // Manejo de errores de validación (@Valid, @NotBlank)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String campo = ((FieldError) error).getField();
            String mensaje = error.getDefaultMessage();
            errores.put(campo, mensaje);
        });
        
        return new ResponseEntity<>(errores, HttpStatus.BAD_REQUEST);
    }
    
    //Manejo peticiones invalidas
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDetalles> handleBadRequestException(
            BadRequestException ex, 
            WebRequest request) {
        
        ErrorDetalles error = new ErrorDetalles(
            LocalDateTime.now(),
            ex.getMessage(),
            request.getDescription(false),
            "PETICION_INVALIDA"
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST); // HTTP 400
    }
    
    //manejejor errores feign
    @ExceptionHandler(feign.FeignException.class)
    public ResponseEntity<ErrorDetalles> handleFeignException(
            feign.FeignException ex, 
            WebRequest request) {
        
        String mensajePersonalizado = "El servicio de Gestión Institucional no está disponible o devolvió un error.";
        
        ErrorDetalles error = new ErrorDetalles(
            LocalDateTime.now(),
            mensajePersonalizado,
            request.getDescription(false),
            "ERROR_COMUNICACION_MICROSERVICIOS"
        );
        
        // Devolvemos 503 (Service Unavailable) porque es un fallo de infraestructura
        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDetalles> handleBadCredentialsException(
            BadCredentialsException ex, 
            WebRequest request) {
        
        ErrorDetalles error = new ErrorDetalles(
            LocalDateTime.now(),
            "Correo o contraseña incorrectos", // Mensaje amigable para el frontend
            request.getDescription(false),
            "CREDenciales_INVALIDAS"
        );
        
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED); // HTTP 401
    }


    /*
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetalles> handleGlobalException(
            Exception ex, 
            WebRequest request) {
        
        ErrorDetalles error = new ErrorDetalles(
            LocalDateTime.now(),
            "Ocurrió un error inesperado en el servidor",
            request.getDescription(false),
            "INTERNAL_SERVER_ERROR"
        );
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }*/
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetalles> handleGlobalException(
            Exception ex, 
            WebRequest request) {
        
        // 1. ESTO IMPRIMIRÁ TODA LA RAÍZ DEL ERROR EN LA CONSOLA DE AUTH
        System.err.println("ERROR INTERNO DETECTADO: " + ex.getClass().getSimpleName());
        ex.printStackTrace(); 
        
        ErrorDetalles error = new ErrorDetalles(
            LocalDateTime.now(),
            // 2. ESTO DEVOLVERÁ EL MENSAJE REAL A LIBRERÍA / POSTMAN EN LUGAR DEL MENSAJE GENÉRICO
            "Error real: " + ex.getMessage(), 
            request.getDescription(false),
            "INTERNAL_SERVER_ERROR"
        );
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}