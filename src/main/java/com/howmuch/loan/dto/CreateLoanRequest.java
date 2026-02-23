package com.howmuch.loan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateLoanRequest(
        @NotNull @DecimalMin("1000.00") BigDecimal amount,
        @NotNull @DecimalMin("1.0") BigDecimal interestRate,
        @NotNull Integer durationMonths
) {
}
