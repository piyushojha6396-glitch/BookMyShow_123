package com.cfs.BMS.controller;

import com.cfs.BMS.dto.CityRequest;
import com.cfs.BMS.dto.CityResponse;
import com.cfs.BMS.service.CityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @GetMapping
    public ResponseEntity<List<CityResponse>> getAllCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CityResponse> getCityById(@PathVariable Long id) {
        return ResponseEntity.ok(cityService.getCityById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CityResponse> addCity(@Valid @RequestBody CityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cityService.addCity(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CityResponse> updateCity(@PathVariable Long id, @Valid @RequestBody CityRequest request) {
        return ResponseEntity.ok(cityService.updateCity(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCity(@PathVariable Long id) {
        cityService.deleteCity(id);
        return ResponseEntity.noContent().build();
    }
}
