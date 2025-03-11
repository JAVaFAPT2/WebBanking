package account.fundtransfer.service;


import account.fundtransfer.models.dto.Account;
import account.fundtransfer.models.dto.FundTranferDTO;
import account.fundtransfer.models.dto.request.FundTransferRequest;
import account.fundtransfer.models.dto.response.FundTranferResponse;

import java.math.BigDecimal;
import java.util.List;

public interface FundTranferService {
    FundTranferResponse fundTranfer(FundTransferRequest fundTranferRequest);
    FundTranferDTO getFundTranferDetailsFromReferenceId(String referenceId);
    List<FundTranferDTO> getAllFundTranferByAccountId(String accountId);
    String internalTransfer(Account fromAccount, Account toAccount, BigDecimal amount);
}
