package com.cfs.BMS.controller;

import com.cfs.BMS.dto.PageResponse;
import com.cfs.BMS.dto.TheaterRequest;
import com.cfs.BMS.dto.TheaterResponse;
import com.cfs.BMS.service.TheaterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theaters")
@RequiredArgsConstructor
public class TheaterController {

    private final TheaterService theaterService;

    @GetMapping
    public ResponseEntity<PageResponse<TheaterResponse>> getAllTheaters(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(theaterService.getAllTheaters(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TheaterResponse> getTheaterById(@PathVariable Long id) {
        return ResponseEntity.ok(theaterService.getTheaterById(id));
    }

    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<TheaterResponse>> getTheaterByCity(@PathVariable Long cityId) {
        return ResponseEntity.ok(theaterService.getTheaterByCity(cityId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TheaterResponse> addTheater(@Valid @RequestBody TheaterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(theaterService.addTheater(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TheaterResponse> updateTheater(@PathVariable Long id,
                                                         @Valid @RequestBody TheaterRequest request) {
        return ResponseEntity.ok(theaterService.updateTheater(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTheater(@PathVariable Long id) {
        theaterService.deleteTheater(id);
        return ResponseEntity.noContent().build();
    }
}
