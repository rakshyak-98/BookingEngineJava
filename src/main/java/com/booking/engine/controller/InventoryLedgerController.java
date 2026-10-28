package com.booking.engine.controller;

import com.booking.engine.dto.InventoryBalanceDTO;
import com.booking.engine.dto.InventoryTraceDTO;
import com.booking.engine.dto.LedgerEntryDTO;
import com.booking.engine.dto.LedgerEntryRequest;
import com.booking.engine.dto.ReversalRequest;
import com.booking.engine.service.InventoryLedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/** Append-only by design: there are no PUT, PATCH or DELETE endpoints. Corrections are reversals. */
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Ledger", description = "Append-only inventory ledger")
public class InventoryLedgerController {

    private final InventoryLedgerService inventoryLedgerService;

    @PostMapping("/{inventoryKey}/entries")
    @Operation(summary = "Record a ledger entry", description = "Append a RECEIPT, RESERVATION, RELEASE or ADJUSTMENT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Entry appended (or the original returned for a repeated idempotency key)"),
        @ApiResponse(responseCode = "400", description = "Invalid entry"),
        @ApiResponse(responseCode = "409", description = "Insufficient inventory or concurrent write; retry with the same idempotency key")
    })
    public ResponseEntity<LedgerEntryDTO> record(
        @PathVariable @Parameter(description = "Inventory key, e.g. ROOM-DELUXE:2026-11-01") String inventoryKey,
        @Valid @RequestBody LedgerEntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryLedgerService.record(inventoryKey, request));
    }

    @PostMapping("/entries/{entryId}/reversal")
    @Operation(summary = "Reverse a ledger entry", description = "Append a compensating entry that negates an earlier one")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Reversal appended"),
        @ApiResponse(responseCode = "404", description = "Entry not found"),
        @ApiResponse(responseCode = "409", description = "Already reversed, or reversal would make inventory negative")
    })
    public ResponseEntity<LedgerEntryDTO> reverse(
        @PathVariable @Parameter(description = "Ledger entry ID") Long entryId,
        @Valid @RequestBody ReversalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryLedgerService.reverse(entryId, request));
    }

    @GetMapping("/{inventoryKey}/balance")
    @Operation(summary = "Get balance", description = "Current balance, or the balance at a point in time")
    public ResponseEntity<InventoryBalanceDTO> getBalance(
        @PathVariable String inventoryKey,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @Parameter(description = "ISO-8601 instant, e.g. 2026-10-29T10:00:00Z") Instant asOf) {
        return ResponseEntity.ok(inventoryLedgerService.getBalance(inventoryKey, asOf));
    }

    @GetMapping("/{inventoryKey}/entries")
    @Operation(summary = "List ledger entries", description = "Entries in sequence order")
    public ResponseEntity<Page<LedgerEntryDTO>> getLedger(
        @PathVariable String inventoryKey,
        @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(inventoryLedgerService.getLedger(inventoryKey, pageable));
    }

    @GetMapping("/{inventoryKey}/trace")
    @Operation(summary = "Trace ledger", description = "Replay every entry from zero and verify sequence, hash chain and balances")
    public ResponseEntity<InventoryTraceDTO> trace(@PathVariable String inventoryKey) {
        return ResponseEntity.ok(inventoryLedgerService.trace(inventoryKey));
    }
}
