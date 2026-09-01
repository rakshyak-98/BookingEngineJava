package com.booking.engine.dto;

import com.booking.engine.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDTO {

    private Long id;
    private String bookingNumber;
    private Long userId;
    private String serviceType;
    private String bookingDate;
    private String startDateTime;
    private String endDateTime;
    private Booking.BookingStatus status;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private Booking.PaymentStatus paymentStatus;
    private String notes;
    private String createdAt;
    private String updatedAt;
}
