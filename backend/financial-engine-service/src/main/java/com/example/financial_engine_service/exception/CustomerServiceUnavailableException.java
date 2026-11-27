package com.example.financial_engine_service.exception;

public class CustomerServiceUnavailableException extends RuntimeException {
    public CustomerServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
