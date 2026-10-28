package com.booking.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryBalanceDTO {

    private String inventoryKey;
    private long balance;
    /** Null when the balance is the current one. */
    private Instant asOf;
    /** Sequence number of the head entry; only set for the current balance. */
    private Long lastSequenceNumber;
}
