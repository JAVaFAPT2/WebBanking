package test.projectcv.service.impl;

import test.projectcv.dto.TransactionDto;
import test.projectcv.service.TransactionService;

import java.util.List;

public class TransactionServiceImpl implements TransactionService {
    @Override
    public TransactionDto createTransaction(TransactionDto transactionDto) {
        return null;
    }

    @Override
    public List<TransactionDto> getTransactionsForAccount(Long accountId) {
        return List.of();
    }
}
