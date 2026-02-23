package com.howmuch.loan.repository;

import com.howmuch.loan.entity.Loan;
import com.howmuch.loan.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {

    List<Loan> findByBorrowerId(UUID borrowerId);

    List<Loan> findByStatus(LoanStatus status);
}
