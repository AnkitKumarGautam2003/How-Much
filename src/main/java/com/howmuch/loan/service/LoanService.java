package com.howmuch.loan.service;

import com.howmuch.loan.dto.CreateLoanRequest;
import com.howmuch.loan.dto.LoanResponse;

public interface LoanService {

    LoanResponse createLoan(CreateLoanRequest request, String borrowerEmail);
}
