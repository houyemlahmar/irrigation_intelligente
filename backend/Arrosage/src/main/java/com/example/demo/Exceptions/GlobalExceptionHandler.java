package com.example.demo.Exceptions;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	// Gestion des erreurs de validation
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidationExceptions(
			MethodArgumentNotValidException ex, WebRequest request) {
		
		Map<String, Object> response = new HashMap<>();
		Map<String, String> errors = new HashMap<>();
		
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});
		
		response.put("timestamp", LocalDateTime.now());
		response.put("status", HttpStatus.BAD_REQUEST.value());
		response.put("error", "Erreur de validation");
		response.put("details", errors);
		response.put("path", request.getDescription(false));
		
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	// Gestion des ressources non trouvées
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
			ResourceNotFoundException ex, WebRequest request) {
		
		Map<String, Object> response = new HashMap<>();
		response.put("timestamp", LocalDateTime.now());
		response.put("status", HttpStatus.NOT_FOUND.value());
		response.put("error", "Ressource non trouvée");
		response.put("message", ex.getMessage());
		response.put("path", request.getDescription(false));
		
		return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
	}
	
	// Gestion des erreurs métier
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<Map<String, Object>> handleBusinessException(
			BusinessException ex, WebRequest request) {
		
		Map<String, Object> response = new HashMap<>();
		response.put("timestamp", LocalDateTime.now());
		response.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
		response.put("error", "Erreur métier");
		response.put("message", ex.getMessage());
		response.put("path", request.getDescription(false));
		
		return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
	}
	
	// Gestion des erreurs de communication inter-services
	@ExceptionHandler(ServiceCommunicationException.class)
	public ResponseEntity<Map<String, Object>> handleServiceCommunicationException(
			ServiceCommunicationException ex, WebRequest request) {
		
		Map<String, Object> response = new HashMap<>();
		response.put("timestamp", LocalDateTime.now());
		response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
		response.put("error", "Service indisponible");
		response.put("message", ex.getMessage());
		response.put("path", request.getDescription(false));
		
		return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
	}
	
	// Gestion des exceptions générales
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGlobalException(
			Exception ex, WebRequest request) {
		
		Map<String, Object> response = new HashMap<>();
		response.put("timestamp", LocalDateTime.now());
		response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
		response.put("error", "Erreur interne du serveur");
		response.put("message", ex.getMessage());
		response.put("path", request.getDescription(false));
		
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
