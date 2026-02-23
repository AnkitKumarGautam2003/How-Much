package com.howmuch.loan.dto;

import com.howmuch.loan.entity.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class LoanResponse {

    private final UUID id;
    private final UUID borrowerId;
    private final BigDecimal amount;
    private final BigDecimal interestRate;
    private final Integer durationMonths;
    private final LoanStatus status;
    private final UUID lenderId;
    private final LocalDateTime fundedAt;
    private final LocalDateTime createdAt;

    public LoanResponse(
            UUID id,
            UUID borrowerId,
            BigDecimal amount,
            BigDecimal interestRate,
            Integer durationMonths,
            LoanStatus status,
            UUID lenderId,
            LocalDateTime fundedAt,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.borrowerId = borrowerId;
        this.amount = amount;
        this.interestRate = interestRate;
        this.durationMonths = durationMonths;
        this.status = status;
        this.lenderId = lenderId;
        this.fundedAt = fundedAt;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBorrowerId() {
        return borrowerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public Integer getDurationMonths() {
        return durationMonths;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public UUID getLenderId() {
        return lenderId;
    }

    public LocalDateTime getFundedAt() {
        return fundedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
