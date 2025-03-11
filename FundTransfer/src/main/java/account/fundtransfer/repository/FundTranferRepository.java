package account.fundtransfer.repository;

import account.fundtransfer.models.entity.FundTranfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface FundTranferRepository extends JpaRepository<FundTranfer, Long> {
    List<FundTranfer> findAllByAccountId(String accountId);
    Optional<FundTranfer> findFundTranfersByTransactionReference(String transactionReference);
    List<FundTranfer> findAllByAccountIdAndStatus(String accountId, String status);
    List<FundTranfer> findFundTranfersByAccountId(String AccountId);
}
