package service.accountservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import service.accountservice.query.internal.model.AccountReadModel;

import java.util.Optional;
import java.util.UUID;

public interface AccountReadRepository extends JpaRepository<AccountReadModel, UUID> {
    Optional<AccountReadModel> findByAccountNumber(String accountNumber);
}
