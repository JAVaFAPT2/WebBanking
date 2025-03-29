package service.accountservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import service.accountservice.command.internal.model.AccountWriteModel;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountWriteRepository extends JpaRepository<AccountWriteModel, UUID> {

    Optional<AccountWriteModel> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    @Modifying
    @Query("UPDATE AccountWriteModel a SET a.balance = :balance WHERE a.accountNumber = :accountNumber")
    int updateBalance(@Param("accountNumber") String accountNumber, @Param("balance") BigDecimal balance);

    @Modifying
    @Query("UPDATE AccountWriteModel a SET a.status = :status WHERE a.accountNumber = :accountNumber")
    int updateStatus(@Param("accountNumber") String accountNumber, @Param("status") String status);

    @Modifying
    @Query("UPDATE AccountWriteModel a SET a.balance = :balance, a.status = :status WHERE a.accountNumber = :accountNumber")
    int updateBalanceAndStatus(@Param("accountNumber") String accountNumber,
                               @Param("balance") BigDecimal balance,
                               @Param("status") String status);

    void deleteByAccountNumber(String accountNumber);
}
