package com.domus.server.common.web;

import com.domus.server.common.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class SecurityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(SecurityExceptionHandler.class);

    private final ObjectMapper objectMapper;

    public SecurityExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void writeUnauthorized(HttpServletRequest request, HttpServletResponse response) throws IOException {
        write(request, response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication is required to access this resource.");
    }

    public void writeForbidden(HttpServletRequest request, HttpServletResponse response) throws IOException {
        write(request, response, HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have permission to access this resource.");
    }

    private void write(
        HttpServletRequest request,
        HttpServletResponse response,
        HttpStatus status,
        String code,
        String message
    ) throws IOException {
        log.warn("Security rejection with status {} on {} {}", status.value(), request.getMethod(), request.getRequestURI());

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(
            response.getWriter(),
            new ApiError(
                code,
                message,
                List.of(),
                Instant.now(),
                request.getRequestURI(),
                correlationId(request)
            )
        );
    }

    private String correlationId(HttpServletRequest request) {
        Object value = request.getAttribute(RequestCorrelation.REQUEST_ATTRIBUTE);
        return value == null ? null : value.toString();
    }
}
