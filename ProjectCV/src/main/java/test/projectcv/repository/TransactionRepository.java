package test.projectcv.repository;

import test.projectcv.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Example query: find all transactions for a specific account.
    List<Transaction> findByAccountId(Long accountId);
}