package com.careerops.app.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record ErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            String path
    ) {}

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message, String path) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                LocalDateTime.now(), status.value(), error, message, path));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex,
                                                        HttpServletRequest req) {
        log.warn("Resource not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex,
                                                          HttpServletRequest req) {
        log.warn("Bad request: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex,
                                                           HttpServletRequest req) {
        log.warn("Malformed request body at {}: {}", req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Invalid request payload format", "Invalid request payload format", req.getRequestURI());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex,
                                                            HttpServletRequest req) {
        log.warn("Unauthorized error at {}: {}", req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler({ConflictException.class, DuplicateResourceException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex,
                                                        HttpServletRequest req) {
        log.warn("Conflict error at {}: {}", req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRequestException ex,
                                                              HttpServletRequest req) {
        log.warn("Invalid request error at {}: {}", req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex,
                                                                HttpServletRequest req) {
        Map<String, String> fieldErrors = new HashMap<>();
        String firstErrorMessage = "Validation failed";
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
            if ("Validation failed".equals(firstErrorMessage)) {
                firstErrorMessage = fe.getField() + ": " + fe.getDefaultMessage();
            }
        }
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 400);
        body.put("error", "Validation Failed");
        body.put("message", firstErrorMessage);
        body.put("fieldErrors", fieldErrors);
        body.put("path", req.getRequestURI());
        log.warn("Validation failed at {}: {}", req.getRequestURI(), fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
                                                            HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Forbidden", "Access denied", req.getRequestURI());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuth(AuthenticationException ex,
                                                    HttpServletRequest req) {
        log.warn("Authentication failure at {}: {}", req.getRequestURI(), ex.getMessage());
        String msg = (ex instanceof BadCredentialsException) ? "Invalid email or password" : "Authentication required";
        return build(HttpStatus.UNAUTHORIZED, msg, msg, req.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                                                             HttpServletRequest req) {
        log.warn("Data integrity violation at {}: {}", req.getRequestURI(), ex.getMessage());
        String msg = "A data constraint was violated.";
        String exMsg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        if (exMsg.contains("uk_candidate_opportunity")) {
            msg = "You have already tracked this opportunity.";
        } else if (exMsg.contains("email") || exMsg.contains("candidate")) {
            msg = "An account with this email already exists.";
        }
        return build(HttpStatus.CONFLICT, msg, msg, req.getRequestURI());
    }

    @ExceptionHandler({DataAccessException.class, CannotCreateTransactionException.class})
    public ResponseEntity<ErrorResponse> handleDatabaseUnavailable(Exception ex, HttpServletRequest req) {
        log.error("Database service error at {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Database service is currently unavailable",
                "Database service is currently unavailable",
                req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception at {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred. Please try again.",
                "An unexpected server error occurred. Please try again.",
                req.getRequestURI());
    }
}
