package com.cfs.BMS.dto;

import com.cfs.BMS.enums.SeatType;

import java.math.BigDecimal;

public record SeatAvailabilityResponse(Long seatId, String seatNumber, String row, Integer col,
                                       SeatType seatType, BigDecimal price, boolean available) {
}
