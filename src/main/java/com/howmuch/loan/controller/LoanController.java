package com.howmuch.loan.controller;

import com.howmuch.loan.dto.CreateLoanRequest;
import com.howmuch.loan.dto.LoanResponse;
import com.howmuch.loan.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }


    @GetMapping
    public List<LoanResponse> getLoans(Authentication authentication) {
        return loanService.getLoansForUser(authentication);
    }

    @PostMapping
    @PreAuthorize("hasRole('BORROWER')")
    public LoanResponse createLoan(@Valid @RequestBody CreateLoanRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String borrowerEmail = authentication.getName();
        return loanService.createLoan(request, borrowerEmail);
    }

    @PostMapping("/{id}/fund")
    public LoanResponse fundLoan(@PathVariable UUID id,
                                 Authentication authentication) {
        return loanService.fundLoan(id, authentication);
    }
}
