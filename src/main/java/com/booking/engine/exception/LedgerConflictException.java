package com.booking.engine.exception;

public class LedgerConflictException extends RuntimeException {

    public LedgerConflictException(String message) {
        super(message);
    }

    public LedgerConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
