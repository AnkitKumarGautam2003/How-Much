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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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


    @Override
    @Transactional
    public LoanResponse fundLoan(UUID loanId, Authentication authentication) {
        String email = authentication.getName();

        User lender = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (lender.getRole() != Role.LENDER) {
            throw new RuntimeException("Only lenders can fund loans");
        }

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new RuntimeException("Loan is not available for funding");
        }

        if (loan.getBorrowerId().equals(lender.getId())) {
            throw new RuntimeException("Cannot fund your own loan");
        }

        loan.setStatus(LoanStatus.FUNDED);
        loan.setLenderId(lender.getId());
        loan.setFundedAt(LocalDateTime.now());

        Loan saved = loanRepository.save(loan);

        return mapToResponse(saved);
    }

    private LoanResponse mapToResponse(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getBorrowerId(),
                loan.getAmount(),
                loan.getInterestRate(),
                loan.getDurationMonths(),
                loan.getStatus(),
                loan.getLenderId(),
                loan.getFundedAt(),
                loan.getCreatedAt()
        );
    }

}
