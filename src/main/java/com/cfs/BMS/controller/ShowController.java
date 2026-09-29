package com.cfs.BMS.controller;

import com.cfs.BMS.dto.*;
import com.cfs.BMS.service.ShowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;

    @GetMapping
    public ResponseEntity<PageResponse<ShowResponse>> getAllShows(
            @PageableDefault(size = 20, sort = {"showDate", "startTime"}) Pageable pageable) {
        return ResponseEntity.ok(showService.getAllShows(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShowResponse> getShowById(@PathVariable Long id) {
        return ResponseEntity.ok(showService.getShowById(id));
    }

    /** Shows of a movie, optionally narrowed by date and/or city. */
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ShowResponse>> getShowByMovie(
            @PathVariable Long movieId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long cityId) {
        return ResponseEntity.ok(showService.getShowByMovie(movieId, date, cityId));
    }

    @GetMapping("/movie/{movieId}/date")
    public ResponseEntity<List<ShowResponse>> getShowByMovieAndDate(
            @PathVariable Long movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(showService.getShowByMovie(movieId, date, null));
    }

    @GetMapping("/theater/{theaterId}")
    public ResponseEntity<List<ShowResponse>> getShowByTheater(
            @PathVariable Long theaterId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(showService.getShowByTheater(theaterId, date));
    }

    @GetMapping("/{id}/available-seats")
    public ResponseEntity<List<SeatResponse>> getAvailableSeats(@PathVariable Long id) {
        return ResponseEntity.ok(showService.getAvailableSeats(id));
    }

    /** Full seat map with price and availability per seat. */
    @GetMapping("/{id}/seat-map")
    public ResponseEntity<List<SeatAvailabilityResponse>> getSeatMap(@PathVariable Long id) {
        return ResponseEntity.ok(showService.getSeatMap(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShowResponse> addShow(@Valid @RequestBody ShowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(showService.addShow(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShowResponse> updateShow(@PathVariable Long id, @Valid @RequestBody ShowRequest request) {
        return ResponseEntity.ok(showService.updateShow(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteShow(@PathVariable Long id) {
        showService.deleteShow(id);
        return ResponseEntity.noContent().build();
    }
}
