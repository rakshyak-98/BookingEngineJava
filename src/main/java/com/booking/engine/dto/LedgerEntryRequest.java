package com.booking.engine.dto;

import com.booking.engine.entity.InventoryLedgerEntry;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerEntryRequest {

    @NotNull
    private InventoryLedgerEntry.EntryType entryType;

    /** Positive magnitude for RECEIPT/RESERVATION/RELEASE; signed and non-zero for ADJUSTMENT. */
    @NotNull
    private Long quantity;

    @Size(max = 50)
    private String referenceType;

    @Size(max = 100)
    private String referenceId;

    @Size(max = 500)
    private String reason;

    /** Client-generated; resending the same key returns the original entry instead of appending again. */
    @NotBlank
    @Size(max = 100)
    private String idempotencyKey;
}
