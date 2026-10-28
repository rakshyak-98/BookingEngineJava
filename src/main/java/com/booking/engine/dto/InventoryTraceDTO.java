package com.booking.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTraceDTO {

    private String inventoryKey;
    private long entriesReplayed;
    /** Sum of every delta, replayed from zero. This is the authoritative quantity. */
    private long replayedBalance;
    /** {@code balanceAfter} of the head entry, i.e. what fast reads report. */
    private long recordedBalance;
    private boolean consistent;
    private Long firstDiscrepancySequence;
    private String discrepancy;
}
