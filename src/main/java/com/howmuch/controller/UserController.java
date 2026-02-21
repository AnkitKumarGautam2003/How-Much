package com.howmuch.controller;

import com.howmuch.dto.KycUpdateRequest;
import com.howmuch.dto.UserResponse;
import com.howmuch.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> listUsers() {
        return userService.listUsers();
    }

    @PatchMapping("/{userId}/kyc")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateKyc(@PathVariable UUID userId, @Valid @RequestBody KycUpdateRequest request) {
        return userService.updateKycStatus(userId, request.kycStatus());
    }
}
