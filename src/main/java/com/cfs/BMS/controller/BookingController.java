package com.cfs.BMS.controller;

import com.cfs.BMS.dto.*;
import com.cfs.BMS.enums.BookingStatus;
import com.cfs.BMS.security.AuthenticatedUser;
import com.cfs.BMS.service.BookingService;
import com.cfs.BMS.service.ShowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final ShowService showService;

    /** The booking always belongs to the authenticated caller. */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@AuthenticationPrincipal AuthenticatedUser caller,
                                                         @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(caller.id(), request));
    }

    @GetMapping("/me")
    public ResponseEntity<PageResponse<BookingResponse>> myBookings(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @PageableDefault(size = 20, sort = "bookedAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(bookingService.getBookingsByUser(caller.id(), caller, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id,
                                                          @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(bookingService.getBookingById(id, caller));
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<BookingResponse> getByReference(@PathVariable String reference,
                                                          @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(bookingService.getBookingByReference(reference, caller));
    }

    /** Owner or admin. */
    @GetMapping("/user/{userId}")
    public ResponseEntity<PageResponse<BookingResponse>> getBookingByUserId(
            @PathVariable Long userId,
            @AuthenticationPrincipal AuthenticatedUser caller,
            @PageableDefault(size = 20, sort = "bookedAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(bookingService.getBookingsByUser(userId, caller, pageable));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id,
                                                         @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, caller));
    }

    // ---- kept for backward compatibility with the old API (public)

    @GetMapping("/show/{showId}/available-seats")
    public ResponseEntity<List<SeatResponse>> getAvailableSeats(@PathVariable Long showId) {
        return ResponseEntity.ok(showService.getAvailableSeats(showId));
    }

    @GetMapping("/show/{showId}/seat-map")
    public ResponseEntity<List<SeatAvailabilityResponse>> getSeatMap(@PathVariable Long showId) {
        return ResponseEntity.ok(showService.getSeatMap(showId));
    }

    // ---- admin

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<BookingResponse>> getAllBookings(
            @RequestParam(required = false) BookingStatus status,
            @PageableDefault(size = 20, sort = "bookedAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(bookingService.getAllBookings(status, pageable));
    }

    @GetMapping("/show/{showId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<BookingResponse>> getBookingsByShow(
            @PathVariable Long showId,
            @PageableDefault(size = 20, sort = "bookedAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(bookingService.getBookingsByShow(showId, pageable));
    }
}
