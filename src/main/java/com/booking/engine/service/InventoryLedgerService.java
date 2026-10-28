package com.booking.engine.service;

import com.booking.engine.dto.InventoryBalanceDTO;
import com.booking.engine.dto.InventoryTraceDTO;
import com.booking.engine.dto.LedgerEntryDTO;
import com.booking.engine.dto.LedgerEntryRequest;
import com.booking.engine.dto.ReversalRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface InventoryLedgerService {

    LedgerEntryDTO record(String inventoryKey, LedgerEntryRequest request);

    LedgerEntryDTO reverse(Long entryId, ReversalRequest request);

    /** Current balance when {@code asOf} is null, otherwise the balance at that instant. */
    InventoryBalanceDTO getBalance(String inventoryKey, Instant asOf);

    Page<LedgerEntryDTO> getLedger(String inventoryKey, Pageable pageable);

    /** Replays the ledger from zero and verifies sequence, hash chain and running balances. */
    InventoryTraceDTO trace(String inventoryKey);
}
