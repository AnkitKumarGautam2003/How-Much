package com.howmuch.dto;

import com.howmuch.domain.KycStatus;
import com.howmuch.domain.Role;

import java.util.UUID;

public record UserResponse(UUID id, String email, Role role, KycStatus kycStatus) {
}
