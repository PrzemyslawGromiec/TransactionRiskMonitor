package org.example.transactionriskmonitor.application.adapter.in.web;

import jakarta.validation.Valid;
import org.example.transactionriskmonitor.application.adapter.in.web.dto.IngestTransactionHttpRequest;
import org.example.transactionriskmonitor.application.adapter.in.web.dto.IngestTransactionHttpResponse;
import org.example.transactionriskmonitor.application.adapter.in.web.mapper.IngestTransactionHttpMapper;
import org.example.transactionriskmonitor.application.port.in.IngestResult;
import org.example.transactionriskmonitor.application.port.in.IngestTransactionCommand;
import org.example.transactionriskmonitor.application.port.in.IngestTransactionUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/* This controller should:
* - expose POST /api/v1/transactions/ingest
* - accept a request DTO
* - map DTO to IngestTransactionCommand
* - cal use case
* - return a clean response DTO
* */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionIngestionController {
    private final IngestTransactionUseCase ingestTransactionUseCase;

    public TransactionIngestionController(IngestTransactionUseCase ingestTransactionUseCase) {
        this.ingestTransactionUseCase = ingestTransactionUseCase;
    }

    @PostMapping("/ingest")
    public ResponseEntity<IngestTransactionHttpResponse> ingest(
            @Valid @RequestBody IngestTransactionHttpRequest request
            ) {
        IngestTransactionCommand command = IngestTransactionHttpMapper.toCommand(request);
        IngestResult result = ingestTransactionUseCase.ingest(command);

        IngestTransactionHttpResponse response = IngestTransactionHttpMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
