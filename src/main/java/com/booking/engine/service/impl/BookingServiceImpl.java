package com.booking.engine.service.impl;

import com.booking.engine.dto.BookingDTO;
import com.booking.engine.entity.Booking;
import com.booking.engine.entity.User;
import com.booking.engine.exception.ResourceNotFoundException;
import com.booking.engine.repository.sql.BookingRepository;
import com.booking.engine.repository.sql.UserRepository;
import com.booking.engine.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    public BookingDTO createBooking(BookingDTO bookingDTO) {
        User user = userRepository.findById(bookingDTO.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + bookingDTO.getUserId()));

        Booking booking = Booking.builder()
            .bookingNumber(generateBookingNumber())
            .user(user)
            .serviceType(bookingDTO.getServiceType())
            .bookingDate(LocalDateTime.now())
            .startDateTime(LocalDateTime.parse(bookingDTO.getStartDateTime()))
            .endDateTime(LocalDateTime.parse(bookingDTO.getEndDateTime()))
            .status(Booking.BookingStatus.PENDING)
            .totalAmount(bookingDTO.getTotalAmount())
            .paidAmount(bookingDTO.getPaidAmount() != null ? bookingDTO.getPaidAmount() : java.math.BigDecimal.ZERO)
            .paymentStatus(Booking.PaymentStatus.PENDING)
            .notes(bookingDTO.getNotes())
            .build();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created: {}", savedBooking.getBookingNumber());
        return mapToDTO(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDTO getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        return mapToDTO(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDTO getBookingByBookingNumber(String bookingNumber) {
        Booking booking = bookingRepository.findByBookingNumber(bookingNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with number: " + bookingNumber));
        return mapToDTO(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDTO> getUserBookings(Long userId, Pageable pageable) {
        return bookingRepository.findByUserId(userId, pageable)
            .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDTO> getBookingsByStatus(Booking.BookingStatus status, Pageable pageable) {
        return bookingRepository.findByStatus(status, pageable)
            .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDTO> getBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return bookingRepository.findByDateRange(startDate, endDate, pageable)
            .map(this::mapToDTO);
    }

    @Override
    public BookingDTO updateBooking(Long id, BookingDTO bookingDTO) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        if (bookingDTO.getServiceType() != null) {
            booking.setServiceType(bookingDTO.getServiceType());
        }
        if (bookingDTO.getNotes() != null) {
            booking.setNotes(bookingDTO.getNotes());
        }

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking updated: {}", updatedBooking.getBookingNumber());
        return mapToDTO(updatedBooking);
    }

    @Override
    public BookingDTO updateBookingStatus(Long id, Booking.BookingStatus status) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        booking.setStatus(status);
        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking status updated to {}: {}", status, updatedBooking.getBookingNumber());
        return mapToDTO(updatedBooking);
    }

    @Override
    public void cancelBooking(Long id) {
        BookingDTO bookingDTO = updateBookingStatus(id, Booking.BookingStatus.CANCELLED);
        log.info("Booking cancelled: {}", bookingDTO.getBookingNumber());
    }

    @Override
    public void deleteBooking(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Booking not found with id: " + id);
        }
        bookingRepository.deleteById(id);
        log.info("Booking deleted with id: {}", id);
    }

    private BookingDTO mapToDTO(Booking booking) {
        return BookingDTO.builder()
            .id(booking.getId())
            .bookingNumber(booking.getBookingNumber())
            .userId(booking.getUser().getId())
            .serviceType(booking.getServiceType())
            .bookingDate(booking.getBookingDate().toString())
            .startDateTime(booking.getStartDateTime().toString())
            .endDateTime(booking.getEndDateTime().toString())
            .status(booking.getStatus())
            .totalAmount(booking.getTotalAmount())
            .paidAmount(booking.getPaidAmount())
            .paymentStatus(booking.getPaymentStatus())
            .notes(booking.getNotes())
            .createdAt(booking.getCreatedAt().toString())
            .updatedAt(booking.getUpdatedAt().toString())
            .build();
    }

    private String generateBookingNumber() {
        return "BK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
