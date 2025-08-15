package io.ghassen.pockito.web;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", Instant.now().toString());
    body.put("status", 400);
    body.put("error", "Bad Request");
    Map<String, String> fields = new HashMap<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      fields.put(fe.getField(), fe.getDefaultMessage());
    }
    body.put("message", "Validation failed");
    body.put("fields", fields);
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Object> handleIllegal(IllegalArgumentException ex) {
    return problem(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<Object> handleNotFound(RuntimeException ex) {
    return problem(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  private ResponseEntity<Object> problem(HttpStatus status, String message) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", Instant.now().toString());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("message", message);
    return ResponseEntity.status(status).body(body);
  }
}
