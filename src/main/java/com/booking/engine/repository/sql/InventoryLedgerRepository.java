package com.booking.engine.repository.sql;

import com.booking.engine.entity.InventoryLedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Append-only access to the ledger. Extends the bare {@link Repository} rather than JpaRepository
 * on purpose: there is no delete, deleteAll or saveAll to call by accident.
 */
@org.springframework.stereotype.Repository
public interface InventoryLedgerRepository extends Repository<InventoryLedgerEntry, Long> {

    InventoryLedgerEntry save(InventoryLedgerEntry entry);

    Optional<InventoryLedgerEntry> findById(Long id);

    Optional<InventoryLedgerEntry> findByIdempotencyKey(String idempotencyKey);

    boolean existsByReversesEntryId(Long reversesEntryId);

    Optional<InventoryLedgerEntry> findTopByInventoryKeyOrderBySequenceNumberDesc(String inventoryKey);

    Page<InventoryLedgerEntry> findByInventoryKeyOrderBySequenceNumberAsc(String inventoryKey, Pageable pageable);

    Stream<InventoryLedgerEntry> streamByInventoryKeyOrderBySequenceNumberAsc(String inventoryKey);

    @Query("SELECT COALESCE(SUM(e.quantityDelta), 0) FROM InventoryLedgerEntry e " +
           "WHERE e.inventoryKey = :inventoryKey AND e.occurredAt <= :asOf")
    long sumDeltaAsOf(@Param("inventoryKey") String inventoryKey, @Param("asOf") Instant asOf);

    /**
     * Net delta of the holds for one reference: RESERVATION/RELEASE rows plus reversals of them.
     * The outstanding hold is the negation of this value.
     */
    @Query("SELECT COALESCE(SUM(e.quantityDelta), 0) FROM InventoryLedgerEntry e " +
           "WHERE e.inventoryKey = :inventoryKey " +
           "AND e.referenceType = :referenceType AND e.referenceId = :referenceId " +
           "AND (e.entryType IN :holdTypes " +
           "     OR e.reversesEntryId IN (SELECT o.id FROM InventoryLedgerEntry o WHERE o.entryType IN :holdTypes))")
    long sumHoldDelta(
        @Param("inventoryKey") String inventoryKey,
        @Param("referenceType") String referenceType,
        @Param("referenceId") String referenceId,
        @Param("holdTypes") Collection<InventoryLedgerEntry.EntryType> holdTypes
    );
}
