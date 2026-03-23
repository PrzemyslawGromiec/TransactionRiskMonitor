package org.example.transactionriskmonitor.domain.exception;

public class DuplicateTransactionPersistenceException extends RuntimeException{
    public DuplicateTransactionPersistenceException(String transactionId, Throwable cause) {
        super("Duplicate transactionId: " + transactionId, cause);
    }
}
