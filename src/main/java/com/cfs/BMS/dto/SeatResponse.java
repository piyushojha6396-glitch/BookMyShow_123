package com.cfs.BMS.dto;

import com.cfs.BMS.enums.SeatType;

public record SeatResponse(Long id, String seatNumber, String row, Integer col, SeatType seatType, Long screenId) {
}
