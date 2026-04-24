package com.domus.server.common.exception;

import com.domus.server.common.web.RequestCorrelation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return build(request, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage(), List.of(), false, exception);
    }

    @ExceptionHandler(UnauthorizedException.class)
    ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException exception, HttpServletRequest request) {
        return build(request, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", exception.getMessage(), List.of(), false, exception);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> handleForbidden(AccessDeniedException exception, HttpServletRequest request) {
        return build(
            request,
            HttpStatus.FORBIDDEN,
            "FORBIDDEN",
            "You do not have permission to access this resource.",
            List.of(),
            false,
            exception
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException exception, HttpServletRequest request) {
        return build(request, HttpStatus.BAD_REQUEST, "BAD_REQUEST", exception.getMessage(), List.of(), false, exception);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<ApiErrorDetail> details = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::toDetail)
            .toList();

        return build(request, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed.", details, false, exception);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException exception, HttpServletRequest request) {
        List<ApiErrorDetail> details = exception.getConstraintViolations()
            .stream()
            .map(violation -> new ApiErrorDetail(violation.getPropertyPath().toString(), violation.getMessage()))
            .collect(Collectors.toList());

        return build(request, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed.", details, false, exception);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
        return build(
            request,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_ERROR",
            "An unexpected error occurred.",
            List.of(),
            true,
            exception
        );
    }

    private ResponseEntity<ApiError> build(
        HttpServletRequest request,
        HttpStatus status,
        String code,
        String message,
        List<ApiErrorDetail> details,
        boolean includeStackTrace,
        Exception exception
    ) {
        if (includeStackTrace) {
            log.error("Request failed with status {} on {}", status.value(), request.getRequestURI(), exception);
        } else if (status.is4xxClientError()) {
            log.warn("Request rejected with status {} on {}: {}", status.value(), request.getRequestURI(), message);
        }

        return ResponseEntity.status(status)
            .body(new ApiError(
                code,
                message,
                details,
                Instant.now(),
                request.getRequestURI(),
                correlationId(request)
            ));
    }

    private ApiErrorDetail toDetail(FieldError error) {
        return new ApiErrorDetail(error.getField(), error.getDefaultMessage());
    }

    private String correlationId(HttpServletRequest request) {
        Object value = request.getAttribute(RequestCorrelation.REQUEST_ATTRIBUTE);
        return value == null ? null : value.toString();
    }
}
