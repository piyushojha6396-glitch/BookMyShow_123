package com.cfs.BMS.service;

import com.cfs.BMS.dto.ChangePasswordRequest;
import com.cfs.BMS.dto.PageResponse;
import com.cfs.BMS.dto.UpdateProfileRequest;
import com.cfs.BMS.dto.UserResponse;
import com.cfs.BMS.entity.User;
import com.cfs.BMS.exception.BadRequestException;
import com.cfs.BMS.exception.ResourceNotFoundException;
import com.cfs.BMS.mapper.EntityMapper;
import com.cfs.BMS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User findEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return EntityMapper.toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAll(Pageable pageable) {
        return PageResponse.of(userRepository.findAll(pageable), EntityMapper::toResponse);
    }

    @Transactional
    public UserResponse updateProfile(Long id, UpdateProfileRequest request) {
        User user = findEntity(id);
        user.setName(request.name().trim());
        user.setPhone(request.phone());
        return EntityMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {
        User user = findEntity(id);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from the current password");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Password changed for user {}", id);
    }

    @Transactional
    public UserResponse setActive(Long id, boolean active, Long actingAdminId) {
        if (!active && id.equals(actingAdminId)) {
            throw new BadRequestException("You cannot deactivate your own account");
        }
        User user = findEntity(id);
        user.setActive(active);
        log.info("User {} {} by admin {}", id, active ? "activated" : "deactivated", actingAdminId);
        return EntityMapper.toResponse(userRepository.save(user));
    }
}
