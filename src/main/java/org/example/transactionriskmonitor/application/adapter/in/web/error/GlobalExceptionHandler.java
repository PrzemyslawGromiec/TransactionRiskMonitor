package org.example.transactionriskmonitor.application.adapter.in.web.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.example.transactionriskmonitor.domain.exception.AccountProfileNotFoundException;
import org.example.transactionriskmonitor.domain.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.InvalidFormatException;


import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(
                    fieldError.getField(),
                    fieldError.getDefaultMessage() != null
                            ? fieldError.getDefaultMessage()
                            : "Invalid value"
            );
        }

        String correlationId = MDC.getCopyOfContextMap() != null ? MDC.getCopyOfContextMap().get("correlationId") : null;
        log.warn("Validation failed. path={}, correlationId={}, fieldErrors={}",
                request.getRequestURI(), correlationId, fieldErrors);

        ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "VALIDATION_FAILED",
                "Request validation failed",
                request.getRequestURI(),
                correlationId,
                fieldErrors
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        String correlationId = getCorrelationId();
        log.warn("Constraint violation. path={}, correlationId={}, message={}",
                request.getRequestURI(), correlationId, ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "VALIDATION_FAILED",
                ex.getMessage(),
                request.getRequestURI(),
                correlationId
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AccountProfileNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountProfileNotFound(
            AccountProfileNotFoundException ex,
            HttpServletRequest request
    ) {
        String correlationId = getCorrelationId();
        log.warn("Account profile not found. path={}, correlationId={}, message={}",
                request.getRequestURI(), correlationId, ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "ACCOUNT_PROFILE_NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI(),
                correlationId
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        Throwable cause = ex.getMostSpecificCause();

        if (cause instanceof InvalidFormatException invalidFormatException) {
            fieldErrors.put("requestBody", buildTypeMessage(invalidFormatException));
        } else {
            fieldErrors.put("requestBody", "Malformed JSON");
        }

        String correlationId = getCorrelationId();
        log.warn("Invalid JSON. path={}, correlationId={}, fieldErrors={}",
                request.getRequestURI(), correlationId, fieldErrors);

        ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "INVALID_JSON",
                "Invalid JSON or invalid field type",
                request.getRequestURI(),
                correlationId,
                fieldErrors
        );

        return ResponseEntity.badRequest().body(response);
    }

    private String buildTypeMessage(InvalidFormatException ex) {
        Class<?> targetType = ex.getTargetType();

        if (targetType == null) {
            return "Invalid value type";
        }

        return switch (targetType.getSimpleName()) {
            case "BigDecimal" -> "Must be a numeric value";
            case "Instant" -> "Must be a valid ISO-8601 timestamp";
            default -> "Invalid value type";
        };
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        String correlationId = getCorrelationId();
        log.error("Unexpected error. path={}, correlationId={}",
                request.getRequestURI(), correlationId, ex);

        ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                request.getRequestURI(),
                correlationId
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request
    ) {
        String correlationId = getCorrelationId();
        log.warn("Bad request. path={}, correlationId={}, message={}",
                request.getRequestURI(), correlationId, ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "BAD_REQUEST",
                ex.getMessage(),
                request.getRequestURI(),
                correlationId
        );

        return ResponseEntity.badRequest().body(response);
    }

    private String getCorrelationId() {
        Map<String, String> context = MDC.getCopyOfContextMap();
        return context != null ? context.get("correlationId") : null;
    }
}
