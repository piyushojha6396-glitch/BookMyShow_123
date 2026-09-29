package com.cfs.BMS.controller;

import com.cfs.BMS.dto.PageResponse;
import com.cfs.BMS.dto.ScreenRequest;
import com.cfs.BMS.dto.ScreenResponse;
import com.cfs.BMS.dto.ScreenUpdateRequest;
import com.cfs.BMS.dto.ShowResponse;
import com.cfs.BMS.service.ScreenService;
import com.cfs.BMS.service.ShowService;
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
@RequestMapping("/api/screens")
@RequiredArgsConstructor
public class ScreenController {

    private final ScreenService screenService;
    private final ShowService showService;

    @GetMapping
    public ResponseEntity<PageResponse<ScreenResponse>> getAllScreens(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(screenService.getAllScreens(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScreenResponse> getScreenById(@PathVariable Long id) {
        return ResponseEntity.ok(screenService.getScreenById(id));
    }

    @GetMapping("/theater/{theaterId}")
    public ResponseEntity<List<ScreenResponse>> getScreenByTheaterId(@PathVariable Long theaterId) {
        return ResponseEntity.ok(screenService.getScreenByTheater(theaterId));
    }

    @GetMapping("/{id}/shows")
    public ResponseEntity<List<ShowResponse>> getShowsOfScreen(@PathVariable Long id) {
        return ResponseEntity.ok(showService.getShowByScreen(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScreenResponse> addScreen(@Valid @RequestBody ScreenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(screenService.addScreen(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScreenResponse> renameScreen(@PathVariable Long id,
                                                       @Valid @RequestBody ScreenUpdateRequest request) {
        return ResponseEntity.ok(screenService.renameScreen(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteScreen(@PathVariable Long id) {
        screenService.deleteScreen(id);
        return ResponseEntity.noContent().build();
    }
}
