package test.projectcv.service;

import test.projectcv.dto.TransactionDto;

import java.util.List;

public interface TransactionService {
    TransactionDto createTransaction(TransactionDto transactionDto);
    List<TransactionDto> getTransactionsForAccount(Long accountId);
}
