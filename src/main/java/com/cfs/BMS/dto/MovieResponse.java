package com.cfs.BMS.dto;

import java.time.LocalDate;

public record MovieResponse(Long id, String title, String description, String genre, String language,
                            Integer durationMinutes, Double rating, LocalDate releaseDate, String posterUrl) {
}
