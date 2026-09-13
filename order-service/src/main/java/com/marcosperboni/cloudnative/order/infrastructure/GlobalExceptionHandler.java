package com.marcosperboni.cloudnative.order.infrastructure;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.marcosperboni.cloudnative.order.domain.DownstreamServiceUnavailableException;
import com.marcosperboni.cloudnative.order.domain.InvalidOrderException;
import com.marcosperboni.cloudnative.order.domain.ResourceNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
			fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
		}
		ErrorResponse body = ErrorResponse.ofValidation(
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				"Validation failed",
				path(request),
				fieldErrors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ErrorResponse.of(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), ex.getMessage(), path(request)));
	}

	@ExceptionHandler(InvalidOrderException.class)
	public ResponseEntity<ErrorResponse> handleInvalidOrder(InvalidOrderException ex, WebRequest request) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(ErrorResponse.of(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(), ex.getMessage(), path(request)));
	}

	@ExceptionHandler(DownstreamServiceUnavailableException.class)
	public ResponseEntity<ErrorResponse> handleDownstreamUnavailable(DownstreamServiceUnavailableException ex, WebRequest request) {
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
				.body(ErrorResponse.of(HttpStatus.BAD_GATEWAY.value(), HttpStatus.BAD_GATEWAY.getReasonPhrase(), ex.getMessage(), path(request)));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest request) {
		log.error("Unexpected error handling request {}", path(request), ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "An unexpected error occurred", path(request)));
	}

	private String path(WebRequest request) {
		String description = request.getDescription(false);
		return description.startsWith("uri=") ? description.substring(4) : description;
	}

}
