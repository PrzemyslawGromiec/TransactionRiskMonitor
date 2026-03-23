package org.example.transactionriskmonitor.domain.exception;

public class AccountProfileNotFoundException extends RuntimeException{
    public AccountProfileNotFoundException(String message) {
        super(message);
    }
}
