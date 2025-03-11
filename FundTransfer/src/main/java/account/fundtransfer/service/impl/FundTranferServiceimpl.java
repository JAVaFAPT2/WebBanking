package account.fundtransfer.service.impl;



import account.fundtransfer.exception.AccountUpdateException;
import account.fundtransfer.exception.GlobalErrolCode;
import account.fundtransfer.exception.InsufficientBalance;
import account.fundtransfer.exception.ResourceNotFound;
import account.fundtransfer.external.AccountService;
import account.fundtransfer.external.TransactionService;
import account.fundtransfer.models.TranferType;
import account.fundtransfer.models.TransactionStatus;
import account.fundtransfer.models.dto.Account;
import account.fundtransfer.models.dto.FundTranferDTO;
import account.fundtransfer.models.dto.Transaction;
import account.fundtransfer.models.dto.request.FundTransferRequest;
import account.fundtransfer.models.dto.response.FundTranferResponse;
import account.fundtransfer.models.entity.FundTranfer;
import account.fundtransfer.models.mapper.FundTranferMapper;
import account.fundtransfer.repository.FundTranferRepository;
import account.fundtransfer.service.FundTranferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundTranferServiceimpl implements FundTranferService {

    private final AccountService accountService;
    private final FundTranferRepository fundTransferRepository;
    private final TransactionService transactionService;

    @Value("${spring.application.ok}")
    private String ok;

    private final FundTranferMapper fundTransferMapper = new FundTranferMapper();


    @Override
    public FundTranferResponse fundTranfer(FundTransferRequest fundTransferRequest) {

        Account fromAccount;
        ResponseEntity<Account> response = accountService.readAccountByAccountNumber(fundTransferRequest.getFromAccountNumber());
        response.getBody();
        fromAccount = response.getBody();
        if (!fromAccount.getAccountStatus().equals("ACTIVE")) {
            log.error("account status is pending or inactive, please update the account status");
            throw new AccountUpdateException("account is status is :pending", GlobalErrolCode.NOT_ACCEPTABLE);
        }
        if (fromAccount.getAccountBalance().compareTo(fundTransferRequest.getAmount()) < 0) {
            log.error("required amount to transfer is not available");
            throw new InsufficientBalance("requested amount is not available", GlobalErrolCode.NOT_ACCEPTABLE);
        }
        Account toAccount;
        response = accountService.readAccountByAccountNumber(fundTransferRequest.getToAccountNumber());
        response.getBody();
        toAccount = response.getBody();
        String transactionId = internalTransfer(fromAccount, toAccount, fundTransferRequest.getAmount());
        FundTranfer fundTransfer = FundTranfer.builder()
                .tranferType(TranferType.INTERNAL)
                .amount(fundTransferRequest.getAmount())
                .fromAccountNumber(fromAccount.getAccountNumber())
                .transactionReference(transactionId)
                .transactionStatus(TransactionStatus.SUCCESS)
                .toAccountNumber(toAccount.getAccountNumber()).build();

        fundTransferRepository.save(fundTransfer);
        return FundTranferResponse.builder()
                .transactionId(transactionId)
                .message("Fund transfer was successful").build();
    }

    /**
     * Transfers funds from one account to another within the system.
     *
     * @param fromAccount The account to transfer funds from.
     * @param toAccount The account to transfer funds to.
     * @param amount The amount of funds to transfer.
     * @return The transaction reference number.
     */
    @Override
    public String internalTransfer(Account fromAccount, Account toAccount, BigDecimal amount) {

        fromAccount.setAccountBalance(fromAccount.getAccountBalance().subtract(amount));
        accountService.updateAccount(fromAccount.getAccountNumber(), fromAccount);

        toAccount.setAccountBalance(toAccount.getAccountBalance().add(amount));
        accountService.updateAccount(toAccount.getAccountNumber(), toAccount);

        List<Transaction> transactions = List.of(
                Transaction.builder()
                        .accountId(fromAccount.getAccountNumber())
                        .transactionType("INTERNAL_TRANSFER")
                        .amount(amount.negate())
                        .description("Internal fund transfer from "+fromAccount.getAccountNumber()+" to "+toAccount.getAccountNumber())
                        .build(),
                Transaction.builder()
                        .accountId(toAccount.getAccountNumber())
                        .transactionType("INTERNAL_TRANSFER")
                        .amount(amount)
                        .description("Internal fund transfer received from: "+fromAccount.getAccountNumber()).build());

        String transactionReference = UUID.randomUUID().toString();
        transactionService.makeInternalTransaction(transactions, transactionReference);
        return transactionReference;
    }

    /**
     * Retrieves the details of a fund transfer based on the given reference ID.
     *
     * @param referenceId The reference ID of the fund transfer.
     * @return The FundTransferDto containing the details of the fund transfer.
     * @throws ResourceNotFound if the fund transfer is not found.
     */
    @Override
    public FundTranferDTO getFundTranferDetailsFromReferenceId(String referenceId) {

        return fundTransferRepository.findFundTranfersByTransactionReference(referenceId)
                .map(fundTransferMapper::convertToDto)
                .orElseThrow(() -> new ResourceNotFound("Fund transfer not found", GlobalErrolCode.NOT_FOUND));
    }

    /**
     * Retrieves a list of fund transfers associated with the given account ID.
     *
     * @param accountId The ID of the account
     * @return A list of fund transfer DTOs
     */
    @Override
    public List<FundTranferDTO> getAllFundTranferByAccountId(String accountId) {

        return fundTransferMapper.convertToDtoList(fundTransferRepository.findFundTranfersByAccountId(accountId));
    }
}
