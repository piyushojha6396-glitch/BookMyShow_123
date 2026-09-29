package com.cfs.BMS.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record ShowResponse(Long id, Long movieId, String movieTitle, Long screenId, String screenName,
                           Long theaterId, String theaterName, Long cityId, String cityName,
                           LocalDate showDate, LocalTime startTime, LocalTime endTime, BigDecimal ticketPrice) {
}
