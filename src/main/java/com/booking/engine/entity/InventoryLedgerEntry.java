package com.booking.engine.entity;

import com.booking.engine.exception.InvalidLedgerEntryException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Objects;

/**
 * One immutable line of the inventory ledger. Rows are only ever inserted: corrections are new
 * REVERSAL rows, never edits. The current quantity of an inventory key is the sum of its deltas,
 * and {@code balanceAfter} caches that running sum so reads don't need a full replay.
 *
 * <p>Each row carries the hash of its predecessor, so an edited, deleted or reordered row breaks
 * the chain and is reported by the ledger trace.
 */
@Entity
@Immutable
@Table(name = "inventory_ledger", uniqueConstraints = {
    @UniqueConstraint(name = "uk_ledger_key_sequence", columnNames = {"inventory_key", "sequence_number"}),
    @UniqueConstraint(name = "uk_ledger_idempotency_key", columnNames = "idempotency_key"),
    @UniqueConstraint(name = "uk_ledger_reverses_entry", columnNames = "reverses_entry_id")
}, indexes = {
    @Index(name = "idx_ledger_reference", columnList = "inventory_key, reference_type, reference_id"),
    @Index(name = "idx_ledger_occurred_at", columnList = "inventory_key, occurred_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class InventoryLedgerEntry {

    public static final String GENESIS_HASH = "0".repeat(64);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inventory_key", nullable = false, updatable = false, length = 100)
    private String inventoryKey;

    /** 1-based, gap-free position within the inventory key. Unique with the key, so it doubles as the concurrency guard. */
    @Column(name = "sequence_number", nullable = false, updatable = false)
    private long sequenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, updatable = false, length = 20)
    private EntryType entryType;

    @Column(name = "quantity_delta", nullable = false, updatable = false)
    private long quantityDelta;

    @Column(name = "balance_after", nullable = false, updatable = false)
    private long balanceAfter;

    @Column(name = "reference_type", updatable = false, length = 50)
    private String referenceType;

    @Column(name = "reference_id", updatable = false, length = 100)
    private String referenceId;

    @Column(name = "reverses_entry_id", updatable = false)
    private Long reversesEntryId;

    @Column(name = "reason", updatable = false, length = 500)
    private String reason;

    @Column(name = "idempotency_key", nullable = false, updatable = false, length = 100)
    private String idempotencyKey;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(name = "previous_hash", nullable = false, updatable = false, length = 64)
    private String previousHash;

    @Column(name = "entry_hash", nullable = false, updatable = false, length = 64)
    private String entryHash;

    /** Returns a copy with {@code entryHash} computed from every other persisted field. */
    public InventoryLedgerEntry seal() {
        return toBuilder().entryHash(computeHash()).build();
    }

    public String computeHash() {
        String payload = String.join("\u001F",
            previousHash,
            inventoryKey,
            Long.toString(sequenceNumber),
            entryType.name(),
            Long.toString(quantityDelta),
            Long.toString(balanceAfter),
            Objects.toString(referenceType, ""),
            Objects.toString(referenceId, ""),
            Objects.toString(reversesEntryId, ""),
            Objects.toString(reason, ""),
            idempotencyKey,
            Long.toString(ChronoUnit.MICROS.between(Instant.EPOCH, occurredAt)));
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required by every JVM", e);
        }
    }

    public enum EntryType {
        /** Stock added, e.g. new rooms or seats released for sale. */
        RECEIPT(1),
        /** Stock held against a reference (usually a booking). */
        RESERVATION(-1),
        /** A previous hold returned to stock. */
        RELEASE(1),
        /** Manual correction; the caller supplies the signed quantity. */
        ADJUSTMENT(0),
        /** System-generated compensating entry that negates exactly one earlier entry. */
        REVERSAL(0);

        private final int sign;

        EntryType(int sign) {
            this.sign = sign;
        }

        public boolean isHold() {
            return this == RESERVATION || this == RELEASE;
        }

        /** Converts a request quantity to a signed delta: positive magnitudes for typed entries, signed non-zero for ADJUSTMENT. */
        public long signedDelta(long quantity) {
            if (sign == 0) {
                if (quantity == 0) {
                    throw new InvalidLedgerEntryException(name() + " quantity must be non-zero");
                }
                return quantity;
            }
            if (quantity <= 0) {
                throw new InvalidLedgerEntryException(name() + " quantity must be positive");
            }
            return sign * quantity;
        }
    }
}
