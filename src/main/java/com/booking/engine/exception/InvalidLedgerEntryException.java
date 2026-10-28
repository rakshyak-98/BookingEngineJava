package com.booking.engine.exception;

public class InvalidLedgerEntryException extends RuntimeException {

    public InvalidLedgerEntryException(String message) {
        super(message);
    }
}
