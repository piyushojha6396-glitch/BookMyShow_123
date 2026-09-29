package com.cfs.BMS.dto;

import com.cfs.BMS.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(Long id, String bookingReference, BookingStatus status, BigDecimal totalPrice,
                              LocalDateTime bookedAt, LocalDateTime cancelledAt, Long userId, String userName,
                              ShowResponse show, List<SeatResponse> seats) {
}
