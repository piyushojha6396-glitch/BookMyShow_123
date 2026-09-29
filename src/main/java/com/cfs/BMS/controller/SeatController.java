package com.cfs.BMS.controller;

import com.cfs.BMS.dto.SeatRequest;
import com.cfs.BMS.dto.SeatResponse;
import com.cfs.BMS.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @GetMapping("/screen/{screenId}")
    public ResponseEntity<List<SeatResponse>> getSeatByScreen(@PathVariable Long screenId) {
        return ResponseEntity.ok(seatService.getSeatsByScreen(screenId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeatResponse> getSeatById(@PathVariable Long id) {
        return ResponseEntity.ok(seatService.getSeatById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeatResponse> addSeat(@Valid @RequestBody SeatRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seatService.addSeat(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSeat(@PathVariable Long id) {
        seatService.deleteSeat(id);
        return ResponseEntity.noContent().build();
    }
}
