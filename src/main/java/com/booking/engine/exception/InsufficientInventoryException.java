package com.booking.engine.exception;

public class InsufficientInventoryException extends RuntimeException {

    public InsufficientInventoryException(String inventoryKey, long available, long requestedDelta) {
        super("Inventory " + inventoryKey + " has " + available + " available; a change of "
            + requestedDelta + " would make it negative");
    }
}
