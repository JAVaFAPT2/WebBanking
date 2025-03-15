package org.loanservice.repository;

import org.loanservice.model.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    Optional<Loan> findLoanByLoanType(String loanType);
    List<Loan> findLoansByTerm(int term);
    List<Loan> findLoansByLoanType(String loanType);
    Optional<Loan> findByMobileNumber(String mobileNumber);
    Optional<Loan> findByLoanNumber(String loanNumber);
}
