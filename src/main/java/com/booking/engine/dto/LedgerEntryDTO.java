package com.booking.engine.dto;

import com.booking.engine.entity.InventoryLedgerEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerEntryDTO {

    private Long id;
    private String inventoryKey;
    private long sequenceNumber;
    private InventoryLedgerEntry.EntryType entryType;
    private long quantityDelta;
    private long balanceAfter;
    private String referenceType;
    private String referenceId;
    private Long reversesEntryId;
    private String reason;
    private String idempotencyKey;
    private Instant occurredAt;
    private String previousHash;
    private String entryHash;
}
