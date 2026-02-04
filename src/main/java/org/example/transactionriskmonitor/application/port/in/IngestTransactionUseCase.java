package org.example.transactionriskmonitor.application.port.in;

public interface IngestTransactionUseCase {
    IngestResult ingest(IngestTransactionCommand command);
}
