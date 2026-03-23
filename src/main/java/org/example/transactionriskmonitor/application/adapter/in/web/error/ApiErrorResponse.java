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
        Map<String, String> fieldErrors
) {
    public static ApiErrorResponse of(
            int status,
            String error,
            String code,
            String message,
            String path
    ) {
        return new ApiErrorResponse(
                Instant.now(),
                status,
                error,
                code,
                message,
                path,
                Map.of()
        );
    }

    public static ApiErrorResponse of(
            int status,
            String error,
            String code,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {
        return new ApiErrorResponse(
                Instant.now(),
                status,
                error,
                code,
                message,
                path,
                fieldErrors
        );
    }


}
