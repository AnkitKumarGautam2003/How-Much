package com.howmuch.loan.service.impl;

import com.howmuch.domain.Role;
import com.howmuch.domain.User;
import com.howmuch.loan.dto.CreateLoanRequest;
import com.howmuch.loan.dto.LoanResponse;
import com.howmuch.loan.entity.Loan;
import com.howmuch.loan.entity.LoanStatus;
import com.howmuch.loan.repository.LoanRepository;
import com.howmuch.loan.service.LoanService;
import com.howmuch.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        return mapToResponse(saved);
    }
    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getLoansForUser(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Loan> loans;

        if (user.getRole() == Role.BORROWER) {
            loans = loanRepository.findByBorrowerId(user.getId());
        } else if (user.getRole() == Role.LENDER) {
            loans = loanRepository.findByStatus(LoanStatus.PENDING);
        } else {
            throw new RuntimeException("Unsupported role");
        }

        return loans.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private LoanResponse mapToResponse(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getBorrowerId(),
                loan.getAmount(),
                loan.getInterestRate(),
                loan.getDurationMonths(),
                loan.getStatus(),
                loan.getCreatedAt()
        );
    }

}
