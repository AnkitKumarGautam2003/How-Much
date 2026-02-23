package com.howmuch.loan.service;

import com.howmuch.loan.dto.CreateLoanRequest;
import com.howmuch.loan.dto.LoanResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface LoanService {

    LoanResponse createLoan(CreateLoanRequest request, String borrowerEmail);

    List<LoanResponse> getLoansForUser(Authentication authentication);
}
