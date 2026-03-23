package org.example.transactionriskmonitor.application.adapter.in.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(2)
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long startNanos = System.nanoTime();

        String correlationId = getCorrelationId(request);
        String method = request.getMethod();
        String path = request.getRequestURI();
        String query = request.getQueryString();

        log.info(
                "Request started. method={}, path={}, query={}, correlationId={}",
                method,
                path,
                query,
                correlationId
        );

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;

            log.info(
                    "Request finished. method={}, path={}, status={}, durationMs={}, correlationId={}",
                    method,
                    path,
                    response.getStatus(),
                    durationMs,
                    correlationId
            );
        }
    }

    private String getCorrelationId(HttpServletRequest request) {
        Object correlationId = request.getAttribute(CorrelationIdFilter.CORRELATION_ID_HEADER);
        if (correlationId != null) {
            return correlationId.toString();
        }

        var contextMap = MDC.getCopyOfContextMap();
        return contextMap != null ? contextMap.get(CorrelationIdFilter.MDC_KEY) : null;
    }
}
