package com.booking.engine.service;

import com.booking.engine.dto.BookingDTO;
import com.booking.engine.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface BookingService {

    BookingDTO createBooking(BookingDTO bookingDTO);

    BookingDTO getBookingById(Long id);

    BookingDTO getBookingByBookingNumber(String bookingNumber);

    Page<BookingDTO> getUserBookings(Long userId, Pageable pageable);

    Page<BookingDTO> getBookingsByStatus(Booking.BookingStatus status, Pageable pageable);

    Page<BookingDTO> getBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    BookingDTO updateBooking(Long id, BookingDTO bookingDTO);

    BookingDTO updateBookingStatus(Long id, Booking.BookingStatus status);

    void cancelBooking(Long id);

    void deleteBooking(Long id);
}
