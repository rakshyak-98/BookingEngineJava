package com.booking.engine.controller;

import com.booking.engine.dto.BookingDTO;
import com.booking.engine.entity.Booking;
import com.booking.engine.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking management endpoints")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a new booking", description = "Create a new booking in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Booking created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<BookingDTO> createBooking(@RequestBody BookingDTO bookingDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(bookingDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID", description = "Retrieve a booking by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Booking found"),
        @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BookingDTO> getBookingById(
        @PathVariable
        @Parameter(description = "Booking ID") Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @GetMapping("/number/{bookingNumber}")
    @Operation(summary = "Get booking by number", description = "Retrieve a booking by its booking number")
    public ResponseEntity<BookingDTO> getBookingByBookingNumber(
        @PathVariable
        @Parameter(description = "Booking number") String bookingNumber) {
        return ResponseEntity.ok(bookingService.getBookingByBookingNumber(bookingNumber));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user bookings", description = "Retrieve all bookings for a specific user")
    public ResponseEntity<Page<BookingDTO>> getUserBookings(
        @PathVariable
        @Parameter(description = "User ID") Long userId,
        @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(bookingService.getUserBookings(userId, pageable));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get bookings by status", description = "Retrieve bookings filtered by status")
    public ResponseEntity<Page<BookingDTO>> getBookingsByStatus(
        @PathVariable
        @Parameter(description = "Booking status") Booking.BookingStatus status,
        @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(bookingService.getBookingsByStatus(status, pageable));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get bookings by date range", description = "Retrieve bookings within a date range")
    public ResponseEntity<Page<BookingDTO>> getBookingsByDateRange(
        @RequestParam
        @Parameter(description = "Start date (ISO format)") String startDate,
        @RequestParam
        @Parameter(description = "End date (ISO format)") String endDate,
        @ParameterObject Pageable pageable) {
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        return ResponseEntity.ok(bookingService.getBookingsByDateRange(start, end, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update booking", description = "Update an existing booking")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Booking updated successfully"),
        @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BookingDTO> updateBooking(
        @PathVariable Long id,
        @RequestBody BookingDTO bookingDTO) {
        return ResponseEntity.ok(bookingService.updateBooking(id, bookingDTO));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update booking status", description = "Update the status of a booking")
    public ResponseEntity<BookingDTO> updateBookingStatus(
        @PathVariable
        @Parameter(description = "Booking ID") Long id,
        @RequestParam
        @Parameter(description = "New status") Booking.BookingStatus status) {
        return ResponseEntity.ok(bookingService.updateBookingStatus(id, status));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel booking", description = "Cancel an existing booking")
    @ApiResponse(responseCode = "200", description = "Booking cancelled successfully")
    public ResponseEntity<BookingDTO> cancelBooking(
        @PathVariable
        @Parameter(description = "Booking ID") Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete booking", description = "Delete a booking from the system")
    @ApiResponse(responseCode = "204", description = "Booking deleted successfully")
    public ResponseEntity<Void> deleteBooking(
        @PathVariable
        @Parameter(description = "Booking ID") Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }
}
