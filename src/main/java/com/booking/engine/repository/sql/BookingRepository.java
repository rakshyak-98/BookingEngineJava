package com.booking.engine.repository.sql;

import com.booking.engine.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingNumber(String bookingNumber);

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Page<Booking> findByStatus(Booking.BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.startDateTime >= :startDate AND b.endDateTime <= :endDate")
    Page<Booking> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status = :status")
    Page<Booking> findByUserIdAndStatus(
        @Param("userId") Long userId,
        @Param("status") Booking.BookingStatus status,
        Pageable pageable
    );
}
