package com.booking.engine.service;

import com.booking.engine.dto.InventoryTraceDTO;
import com.booking.engine.dto.LedgerEntryDTO;
import com.booking.engine.dto.LedgerEntryRequest;
import com.booking.engine.dto.ReversalRequest;
import com.booking.engine.entity.InventoryLedgerEntry;
import com.booking.engine.entity.InventoryLedgerEntry.EntryType;
import com.booking.engine.exception.InsufficientInventoryException;
import com.booking.engine.exception.InvalidLedgerEntryException;
import com.booking.engine.exception.LedgerConflictException;
import com.booking.engine.mapper.InventoryLedgerMapper;
import com.booking.engine.repository.sql.InventoryLedgerRepository;
import com.booking.engine.service.impl.InventoryLedgerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Inventory Ledger Service Tests")
class InventoryLedgerServiceTest {

    private static final String KEY = "ROOM-DELUXE:2026-11-01";

    @Mock
    private InventoryLedgerRepository ledgerRepository;

    private InventoryLedgerService ledgerService;

    @BeforeEach
    void setUp() {
        ledgerService = new InventoryLedgerServiceImpl(ledgerRepository, Mappers.getMapper(InventoryLedgerMapper.class));
    }

    @Test
    @DisplayName("First receipt starts the chain at sequence 1 from the genesis hash")
    void firstReceiptStartsChain() {
        when(ledgerRepository.findByIdempotencyKey("r1")).thenReturn(Optional.empty());
        when(ledgerRepository.findTopByInventoryKeyOrderBySequenceNumberDesc(KEY)).thenReturn(Optional.empty());
        when(ledgerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LedgerEntryDTO result = ledgerService.record(KEY, request(EntryType.RECEIPT, 10, "r1"));

        assertEquals(1, result.getSequenceNumber());
        assertEquals(10, result.getQuantityDelta());
        assertEquals(10, result.getBalanceAfter());
        assertEquals(InventoryLedgerEntry.GENESIS_HASH, result.getPreviousHash());
        assertNotNull(result.getEntryHash());
    }

    @Test
    @DisplayName("Reservation links to the head entry and lowers the balance")
    void reservationAppendsToHead() {
        InventoryLedgerEntry head = entry(null, 3, EntryType.RECEIPT, 10, 10, "prev");
        when(ledgerRepository.findByIdempotencyKey("res1")).thenReturn(Optional.empty());
        when(ledgerRepository.findTopByInventoryKeyOrderBySequenceNumberDesc(KEY)).thenReturn(Optional.of(head));
        when(ledgerRepository.sumHoldDelta(eq(KEY), eq("BOOKING"), eq("BK1"), anyCollection())).thenReturn(0L);
        when(ledgerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LedgerEntryDTO result = ledgerService.record(KEY, hold(EntryType.RESERVATION, 4, "BK1", "res1"));

        assertEquals(4, result.getSequenceNumber());
        assertEquals(-4, result.getQuantityDelta());
        assertEquals(6, result.getBalanceAfter());
        assertEquals(head.getEntryHash(), result.getPreviousHash());
    }

    @Test
    @DisplayName("Reserving more than is available is rejected and nothing is written")
    void overReservationRejected() {
        InventoryLedgerEntry head = entry(null, 1, EntryType.RECEIPT, 2, 2, InventoryLedgerEntry.GENESIS_HASH);
        when(ledgerRepository.findByIdempotencyKey("res1")).thenReturn(Optional.empty());
        when(ledgerRepository.findTopByInventoryKeyOrderBySequenceNumberDesc(KEY)).thenReturn(Optional.of(head));

        assertThrows(InsufficientInventoryException.class,
            () -> ledgerService.record(KEY, hold(EntryType.RESERVATION, 3, "BK1", "res1")));
        verify(ledgerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Releasing more than the reference holds is rejected")
    void overReleaseRejected() {
        InventoryLedgerEntry head = entry(null, 2, EntryType.RESERVATION, -2, 8, "prev");
        when(ledgerRepository.findByIdempotencyKey("rel1")).thenReturn(Optional.empty());
        when(ledgerRepository.findTopByInventoryKeyOrderBySequenceNumberDesc(KEY)).thenReturn(Optional.of(head));
        when(ledgerRepository.sumHoldDelta(eq(KEY), eq("BOOKING"), eq("BK1"), anyCollection())).thenReturn(-2L);

        assertThrows(InvalidLedgerEntryException.class,
            () -> ledgerService.record(KEY, hold(EntryType.RELEASE, 3, "BK1", "rel1")));
        verify(ledgerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Repeating an idempotency key returns the original entry without appending")
    void idempotentReplay() {
        InventoryLedgerEntry existing = entry(7L, 1, EntryType.RECEIPT, 10, 10, InventoryLedgerEntry.GENESIS_HASH);
        when(ledgerRepository.findByIdempotencyKey("r1")).thenReturn(Optional.of(existing));

        LedgerEntryDTO result = ledgerService.record(KEY, request(EntryType.RECEIPT, 10, "r1"));

        assertEquals(7L, result.getId());
        verify(ledgerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Reusing an idempotency key for a different entry is a conflict")
    void idempotencyKeyMismatch() {
        InventoryLedgerEntry existing = entry(7L, 1, EntryType.RECEIPT, 10, 10, InventoryLedgerEntry.GENESIS_HASH);
        when(ledgerRepository.findByIdempotencyKey("r1")).thenReturn(Optional.of(existing));

        assertThrows(LedgerConflictException.class,
            () -> ledgerService.record(KEY, request(EntryType.RECEIPT, 5, "r1")));
    }

    @Test
    @DisplayName("A lost race on the sequence number surfaces as a retryable conflict")
    void concurrentWriteConflict() {
        when(ledgerRepository.findByIdempotencyKey("r1")).thenReturn(Optional.empty());
        when(ledgerRepository.findTopByInventoryKeyOrderBySequenceNumberDesc(KEY)).thenReturn(Optional.empty());
        when(ledgerRepository.save(any())).thenThrow(new DataIntegrityViolationException("uk_ledger_key_sequence"));

        assertThrows(LedgerConflictException.class,
            () -> ledgerService.record(KEY, request(EntryType.RECEIPT, 10, "r1")));
    }

    @Test
    @DisplayName("Reversal appends the negated delta and points at the original")
    void reversalNegatesOriginal() {
        InventoryLedgerEntry original = entry(5L, 1, EntryType.RECEIPT, 10, 10, InventoryLedgerEntry.GENESIS_HASH);
        when(ledgerRepository.findById(5L)).thenReturn(Optional.of(original));
        when(ledgerRepository.findByIdempotencyKey("rev1")).thenReturn(Optional.empty());
        when(ledgerRepository.existsByReversesEntryId(5L)).thenReturn(false);
        when(ledgerRepository.findTopByInventoryKeyOrderBySequenceNumberDesc(KEY)).thenReturn(Optional.of(original));
        ArgumentCaptor<InventoryLedgerEntry> saved = ArgumentCaptor.forClass(InventoryLedgerEntry.class);
        when(ledgerRepository.save(saved.capture())).thenAnswer(inv -> inv.getArgument(0));

        ledgerService.reverse(5L, new ReversalRequest("Entered twice", "rev1"));

        assertEquals(EntryType.REVERSAL, saved.getValue().getEntryType());
        assertEquals(-10, saved.getValue().getQuantityDelta());
        assertEquals(0, saved.getValue().getBalanceAfter());
        assertEquals(5L, saved.getValue().getReversesEntryId());
    }

    @Test
    @DisplayName("An entry can only be reversed once")
    void doubleReversalRejected() {
        InventoryLedgerEntry original = entry(5L, 1, EntryType.RECEIPT, 10, 10, InventoryLedgerEntry.GENESIS_HASH);
        when(ledgerRepository.findById(5L)).thenReturn(Optional.of(original));
        when(ledgerRepository.findByIdempotencyKey("rev2")).thenReturn(Optional.empty());
        when(ledgerRepository.existsByReversesEntryId(5L)).thenReturn(true);

        assertThrows(LedgerConflictException.class,
            () -> ledgerService.reverse(5L, new ReversalRequest("again", "rev2")));
    }

    @Test
    @DisplayName("Trace of an untouched ledger is consistent and replays to the head balance")
    void traceConsistent() {
        List<InventoryLedgerEntry> chain = chain(
            new Object[]{EntryType.RECEIPT, 10L},
            new Object[]{EntryType.RESERVATION, -4L},
            new Object[]{EntryType.RELEASE, 1L});
        when(ledgerRepository.streamByInventoryKeyOrderBySequenceNumberAsc(KEY)).thenReturn(chain.stream());

        InventoryTraceDTO trace = ledgerService.trace(KEY);

        assertTrue(trace.isConsistent());
        assertEquals(3, trace.getEntriesReplayed());
        assertEquals(7, trace.getReplayedBalance());
        assertEquals(7, trace.getRecordedBalance());
    }

    @Test
    @DisplayName("Trace detects a row edited in place")
    void traceDetectsModifiedEntry() {
        List<InventoryLedgerEntry> chain = chain(
            new Object[]{EntryType.RECEIPT, 10L},
            new Object[]{EntryType.RESERVATION, -4L});
        // Someone edits the reservation's quantity directly in the database.
        chain.set(1, chain.get(1).toBuilder().quantityDelta(-1).build());
        when(ledgerRepository.streamByInventoryKeyOrderBySequenceNumberAsc(KEY)).thenReturn(chain.stream());

        InventoryTraceDTO trace = ledgerService.trace(KEY);

        assertFalse(trace.isConsistent());
        assertEquals(2L, trace.getFirstDiscrepancySequence());
    }

    @Test
    @DisplayName("Trace detects a deleted row")
    void traceDetectsDeletedEntry() {
        List<InventoryLedgerEntry> chain = chain(
            new Object[]{EntryType.RECEIPT, 10L},
            new Object[]{EntryType.RESERVATION, -4L},
            new Object[]{EntryType.RECEIPT, 2L});
        chain.remove(1);
        when(ledgerRepository.streamByInventoryKeyOrderBySequenceNumberAsc(KEY)).thenReturn(chain.stream());

        InventoryTraceDTO trace = ledgerService.trace(KEY);

        assertFalse(trace.isConsistent());
        assertEquals(3L, trace.getFirstDiscrepancySequence());
    }

    private static LedgerEntryRequest request(EntryType type, long quantity, String idempotencyKey) {
        return LedgerEntryRequest.builder().entryType(type).quantity(quantity).idempotencyKey(idempotencyKey).build();
    }

    private static LedgerEntryRequest hold(EntryType type, long quantity, String bookingNumber, String idempotencyKey) {
        return LedgerEntryRequest.builder()
            .entryType(type).quantity(quantity)
            .referenceType("BOOKING").referenceId(bookingNumber)
            .idempotencyKey(idempotencyKey)
            .build();
    }

    private static InventoryLedgerEntry entry(Long id, long sequence, EntryType type, long delta, long balanceAfter, String previousHash) {
        return InventoryLedgerEntry.builder()
            .id(id)
            .inventoryKey(KEY)
            .sequenceNumber(sequence)
            .entryType(type)
            .quantityDelta(delta)
            .balanceAfter(balanceAfter)
            .idempotencyKey("seed-" + sequence)
            .occurredAt(Instant.parse("2026-10-29T10:00:00Z").plus(sequence, ChronoUnit.MINUTES))
            .previousHash(previousHash)
            .build()
            .seal();
    }

    private static List<InventoryLedgerEntry> chain(Object[]... steps) {
        List<InventoryLedgerEntry> entries = new ArrayList<>();
        String previousHash = InventoryLedgerEntry.GENESIS_HASH;
        long balance = 0;
        for (int i = 0; i < steps.length; i++) {
            long delta = (Long) steps[i][1];
            balance += delta;
            InventoryLedgerEntry e = entry((long) i + 1, i + 1, (EntryType) steps[i][0], delta, balance, previousHash);
            entries.add(e);
            previousHash = e.getEntryHash();
        }
        return entries;
    }
}
