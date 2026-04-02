package org.example.transactionriskmonitor.application.adapter.in.web.mapper;

import org.example.transactionriskmonitor.application.adapter.in.web.dto.IngestTransactionHttpRequest;
import org.example.transactionriskmonitor.application.adapter.in.web.dto.IngestTransactionHttpResponse;
import org.example.transactionriskmonitor.application.port.in.IngestResult;
import org.example.transactionriskmonitor.application.port.in.IngestTransactionCommand;

import java.util.Collections;

public class IngestTransactionHttpMapper {
    public static IngestTransactionCommand toCommand(IngestTransactionHttpRequest request) {
        return new IngestTransactionCommand(
                request.transactionId(),
                request.accountId(),
                request.merchantId(),
                request.amount(),
                request.currency(),
                request.country(),
                request.occurredAt()
        );
    }

    public static IngestTransactionHttpResponse toResponse(IngestResult result) {
        return switch (result) {

            case IngestResult.Accepted accepted ->
                new IngestTransactionHttpResponse(
                        accepted.transactionId(),
                        "ACCEPTED",
                        accepted.riskScore().value(),
                        accepted.reasons(),
                        "Transaction accepted"
                );

            case IngestResult.Duplicated duplicated ->
                new IngestTransactionHttpResponse(
                        duplicated.transactionId(),
                        "DUPLICATED",
                        null,
                        Collections.emptySet(),
                        "Transaction already exists"
                );
        };
    }
}
