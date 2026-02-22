package com.howmuch.loan.service.impl;

import com.howmuch.domain.User;
import com.howmuch.loan.dto.CreateLoanRequest;
import com.howmuch.loan.dto.LoanResponse;
import com.howmuch.loan.entity.Loan;
import com.howmuch.loan.repository.LoanRepository;
import com.howmuch.loan.service.LoanService;
import com.howmuch.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    public LoanServiceImpl(LoanRepository loanRepository, UserRepository userRepository) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public LoanResponse createLoan(CreateLoanRequest request, String borrowerEmail) {
        User user = userRepository.findByEmail(borrowerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Loan loan = new Loan(
                user.getId(),
                request.amount(),
                request.interestRate(),
                request.durationMonths()
        );

        Loan saved = loanRepository.save(loan);

        return new LoanResponse(
                saved.getId(),
                saved.getBorrowerId(),
                saved.getAmount(),
                saved.getInterestRate(),
                saved.getDurationMonths(),
                saved.getStatus(),
                saved.getCreatedAt()
        );
    }
}
