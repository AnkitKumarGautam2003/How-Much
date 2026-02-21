package com.howmuch.dto;

import com.howmuch.domain.KycStatus;
import jakarta.validation.constraints.NotNull;

public record KycUpdateRequest(@NotNull KycStatus kycStatus) {
}
