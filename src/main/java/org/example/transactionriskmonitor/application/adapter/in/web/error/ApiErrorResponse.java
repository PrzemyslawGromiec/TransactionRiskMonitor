package org.example.transactionriskmonitor.application.adapter.in.web.error;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        String correlationId,
        Map<String, String> fieldErrors
) {
    public static ApiErrorResponse of(
            int status,
            String error,
            String code,
            String message,
            String path,
            String correlationId
    ) {
        return new ApiErrorResponse(
                Instant.now(),
                status,
                error,
                code,
                message,
                path,
                correlationId,
                Map.of()
        );
    }

    public static ApiErrorResponse of(
            int status,
            String error,
            String code,
            String message,
            String path,
            String correlationId,
            Map<String, String> fieldErrors
    ) {
        return new ApiErrorResponse(
                Instant.now(),
                status,
                error,
                code,
                message,
                path,
                correlationId,
                fieldErrors
        );
    }


}
