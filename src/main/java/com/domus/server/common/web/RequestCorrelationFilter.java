package com.domus.server.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestCorrelationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String correlationId = resolveCorrelationId(request);
        request.setAttribute(RequestCorrelation.REQUEST_ATTRIBUTE, correlationId);
        response.setHeader(RequestCorrelation.HEADER_NAME, correlationId);
        MDC.put(RequestCorrelation.MDC_KEY, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(RequestCorrelation.MDC_KEY);
        }
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String headerValue = request.getHeader(RequestCorrelation.HEADER_NAME);
        return headerValue == null || headerValue.isBlank() ? UUID.randomUUID().toString() : headerValue.trim();
    }
}
