package com.cfs.BMS.controller;

import com.cfs.BMS.dto.*;
import com.cfs.BMS.security.AuthenticatedUser;
import com.cfs.BMS.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(userService.getById(caller.id()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(@AuthenticationPrincipal AuthenticatedUser caller,
                                                 @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(caller.id(), request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<MessageResponse> changePassword(@AuthenticationPrincipal AuthenticatedUser caller,
                                                          @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(caller.id(), request);
        return ResponseEntity.ok(new MessageResponse("Password updated"));
    }

    // ---- admin

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(userService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> deactivate(@PathVariable Long id,
                                                   @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(userService.setActive(id, false, caller.id()));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> activate(@PathVariable Long id,
                                                 @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(userService.setActive(id, true, caller.id()));
    }
}
