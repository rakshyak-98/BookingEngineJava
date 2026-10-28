package com.booking.engine.service.impl;

import com.booking.engine.dto.InventoryBalanceDTO;
import com.booking.engine.dto.InventoryTraceDTO;
import com.booking.engine.dto.LedgerEntryDTO;
import com.booking.engine.dto.LedgerEntryRequest;
import com.booking.engine.dto.ReversalRequest;
import com.booking.engine.entity.InventoryLedgerEntry;
import com.booking.engine.entity.InventoryLedgerEntry.EntryType;
import com.booking.engine.exception.InsufficientInventoryException;
import com.booking.engine.exception.InvalidLedgerEntryException;
import com.booking.engine.exception.LedgerConflictException;
import com.booking.engine.exception.ResourceNotFoundException;
import com.booking.engine.mapper.InventoryLedgerMapper;
import com.booking.engine.repository.sql.InventoryLedgerRepository;
import com.booking.engine.service.InventoryLedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Concurrency: every append reads the head entry and writes {@code head.sequence + 1}. The unique
 * (inventory_key, sequence_number) constraint makes two writers racing on the same key collide, so
 * the loser gets a 409 and retries with the same idempotency key. No row is ever locked or updated.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class InventoryLedgerServiceImpl implements InventoryLedgerService {

    private static final Set<EntryType> HOLD_TYPES = EnumSet.of(EntryType.RESERVATION, EntryType.RELEASE);
    private static final int MAX_KEY_LENGTH = 100;

    private final InventoryLedgerRepository ledgerRepository;
    private final InventoryLedgerMapper ledgerMapper;

    @Override
    public LedgerEntryDTO record(String inventoryKey, LedgerEntryRequest request) {
        validateKey(inventoryKey);
        EntryType type = request.getEntryType();
        if (type == EntryType.REVERSAL) {
            throw new InvalidLedgerEntryException("Reversals are created through the reversal endpoint");
        }
        long delta = type.signedDelta(request.getQuantity());

        Optional<InventoryLedgerEntry> replay = findReplay(request.getIdempotencyKey(), inventoryKey, type, delta);
        if (replay.isPresent()) {
            return ledgerMapper.toDto(replay.get());
        }

        if (type.isHold() && (isBlank(request.getReferenceType()) || isBlank(request.getReferenceId()))) {
            throw new InvalidLedgerEntryException(type + " requires referenceType and referenceId");
        }
        if (type == EntryType.ADJUSTMENT && isBlank(request.getReason())) {
            throw new InvalidLedgerEntryException("ADJUSTMENT requires a reason");
        }

        InventoryLedgerEntry saved = append(inventoryKey, type, delta, type.isHold(),
            request.getReferenceType(), request.getReferenceId(), null,
            request.getReason(), request.getIdempotencyKey());
        return ledgerMapper.toDto(saved);
    }

    @Override
    public LedgerEntryDTO reverse(Long entryId, ReversalRequest request) {
        InventoryLedgerEntry original = ledgerRepository.findById(entryId)
            .orElseThrow(() -> new ResourceNotFoundException("Ledger entry not found with id: " + entryId));
        long delta = -original.getQuantityDelta();

        Optional<InventoryLedgerEntry> replay =
            findReplay(request.getIdempotencyKey(), original.getInventoryKey(), EntryType.REVERSAL, delta);
        if (replay.isPresent()) {
            return ledgerMapper.toDto(replay.get());
        }

        if (original.getEntryType() == EntryType.REVERSAL) {
            throw new InvalidLedgerEntryException("A reversal cannot be reversed; record a new entry instead");
        }
        if (ledgerRepository.existsByReversesEntryId(entryId)) {
            throw new LedgerConflictException("Ledger entry " + entryId + " has already been reversed");
        }

        InventoryLedgerEntry saved = append(original.getInventoryKey(), EntryType.REVERSAL, delta,
            original.getEntryType().isHold(), original.getReferenceType(), original.getReferenceId(),
            original.getId(), request.getReason(), request.getIdempotencyKey());
        return ledgerMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryBalanceDTO getBalance(String inventoryKey, Instant asOf) {
        validateKey(inventoryKey);
        if (asOf != null) {
            return InventoryBalanceDTO.builder()
                .inventoryKey(inventoryKey)
                .balance(ledgerRepository.sumDeltaAsOf(inventoryKey, asOf))
                .asOf(asOf)
                .build();
        }
        Optional<InventoryLedgerEntry> head = ledgerRepository.findTopByInventoryKeyOrderBySequenceNumberDesc(inventoryKey);
        return InventoryBalanceDTO.builder()
            .inventoryKey(inventoryKey)
            .balance(head.map(InventoryLedgerEntry::getBalanceAfter).orElse(0L))
            .lastSequenceNumber(head.map(InventoryLedgerEntry::getSequenceNumber).orElse(null))
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LedgerEntryDTO> getLedger(String inventoryKey, Pageable pageable) {
        validateKey(inventoryKey);
        return ledgerRepository.findByInventoryKeyOrderBySequenceNumberAsc(inventoryKey, pageable)
            .map(ledgerMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryTraceDTO trace(String inventoryKey) {
        validateKey(inventoryKey);
        long expectedSequence = 1;
        String expectedPreviousHash = InventoryLedgerEntry.GENESIS_HASH;
        long replayedBalance = 0;
        long recordedBalance = 0;
        Long discrepancySequence = null;
        String discrepancy = null;

        try (Stream<InventoryLedgerEntry> entries = ledgerRepository.streamByInventoryKeyOrderBySequenceNumberAsc(inventoryKey)) {
            Iterator<InventoryLedgerEntry> it = entries.iterator();
            while (it.hasNext()) {
                InventoryLedgerEntry entry = it.next();
                replayedBalance += entry.getQuantityDelta();
                recordedBalance = entry.getBalanceAfter();

                if (discrepancy == null) {
                    discrepancy = verify(entry, expectedSequence, expectedPreviousHash, replayedBalance);
                    if (discrepancy != null) {
                        discrepancySequence = entry.getSequenceNumber();
                    }
                }
                expectedSequence = entry.getSequenceNumber() + 1;
                expectedPreviousHash = entry.getEntryHash();
            }
        }

        boolean consistent = discrepancy == null;
        if (!consistent) {
            log.error("Inventory ledger {} failed trace at sequence {}: {}", inventoryKey, discrepancySequence, discrepancy);
        }
        return InventoryTraceDTO.builder()
            .inventoryKey(inventoryKey)
            .entriesReplayed(expectedSequence - 1)
            .replayedBalance(replayedBalance)
            .recordedBalance(recordedBalance)
            .consistent(consistent)
            .firstDiscrepancySequence(discrepancySequence)
            .discrepancy(discrepancy)
            .build();
    }

    private String verify(InventoryLedgerEntry entry, long expectedSequence, String expectedPreviousHash, long replayedBalance) {
        if (entry.getSequenceNumber() != expectedSequence) {
            return "expected sequence " + expectedSequence + " but found " + entry.getSequenceNumber() + " (entry missing)";
        }
        if (!entry.getPreviousHash().equals(expectedPreviousHash)) {
            return "previous_hash does not match the preceding entry (chain broken)";
        }
        if (!entry.computeHash().equals(entry.getEntryHash())) {
            return "entry_hash does not match the entry's contents (entry modified)";
        }
        if (entry.getBalanceAfter() != replayedBalance) {
            return "balance_after is " + entry.getBalanceAfter() + " but replay gives " + replayedBalance;
        }
        return null;
    }

    private InventoryLedgerEntry append(String inventoryKey, EntryType type, long delta, boolean affectsHold,
                                        String referenceType, String referenceId, Long reversesEntryId,
                                        String reason, String idempotencyKey) {
        Optional<InventoryLedgerEntry> head = ledgerRepository.findTopByInventoryKeyOrderBySequenceNumberDesc(inventoryKey);
        long available = head.map(InventoryLedgerEntry::getBalanceAfter).orElse(0L);
        long balanceAfter = available + delta;
        if (balanceAfter < 0) {
            throw new InsufficientInventoryException(inventoryKey, available, delta);
        }

        if (affectsHold && referenceType != null && referenceId != null) {
            long outstanding = -ledgerRepository.sumHoldDelta(inventoryKey, referenceType, referenceId, HOLD_TYPES);
            if (outstanding - delta < 0) {
                throw new InvalidLedgerEntryException("Returning " + delta + " to stock exceeds the " + outstanding
                    + " held for " + referenceType + " " + referenceId);
            }
        }

        InventoryLedgerEntry entry = InventoryLedgerEntry.builder()
            .inventoryKey(inventoryKey)
            .sequenceNumber(head.map(InventoryLedgerEntry::getSequenceNumber).orElse(0L) + 1)
            .entryType(type)
            .quantityDelta(delta)
            .balanceAfter(balanceAfter)
            .referenceType(referenceType)
            .referenceId(referenceId)
            .reversesEntryId(reversesEntryId)
            .reason(reason)
            .idempotencyKey(idempotencyKey)
            // Truncated to what DATETIME(6) stores, so the hash still verifies after a round trip.
            .occurredAt(Instant.now().truncatedTo(ChronoUnit.MICROS))
            .previousHash(head.map(InventoryLedgerEntry::getEntryHash).orElse(InventoryLedgerEntry.GENESIS_HASH))
            .build()
            .seal();

        InventoryLedgerEntry saved;
        try {
            saved = ledgerRepository.save(entry);
        } catch (DataIntegrityViolationException ex) {
            throw new LedgerConflictException(
                "Concurrent write to inventory " + inventoryKey + "; retry with the same idempotency key", ex);
        }
        log.info("Inventory {} #{} {} {} -> {}", inventoryKey, saved.getSequenceNumber(), type, delta, balanceAfter);
        return saved;
    }

    private Optional<InventoryLedgerEntry> findReplay(String idempotencyKey, String inventoryKey, EntryType type, long delta) {
        Optional<InventoryLedgerEntry> existing = ledgerRepository.findByIdempotencyKey(idempotencyKey);
        existing.ifPresent(entry -> {
            if (!entry.getInventoryKey().equals(inventoryKey) || entry.getEntryType() != type || entry.getQuantityDelta() != delta) {
                throw new LedgerConflictException("Idempotency key " + idempotencyKey + " was already used for a different entry");
            }
        });
        return existing;
    }

    private void validateKey(String inventoryKey) {
        if (isBlank(inventoryKey) || inventoryKey.length() > MAX_KEY_LENGTH) {
            throw new InvalidLedgerEntryException("inventoryKey must be 1-" + MAX_KEY_LENGTH + " characters");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
