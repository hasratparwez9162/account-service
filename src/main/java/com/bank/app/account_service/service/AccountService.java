package com.bank.app.account_service.service;


import com.bank.app.account_service.entity.Account;
import com.bank.app.account_service.exception.AccountNotFoundException;
import com.bank.core.entity.TransactionRequest;

import java.util.List;

public interface AccountService {

        Account openAccount(Account account);
        List<Account> getAccountsByUserId(Long userId);

        String processTransaction(TransactionRequest transactionRequest);
    String processTransactions(TransactionRequest transactionRequest);
         void validateAccountExists(String accountNumber);

    Account getAccountByAccountNumber(String accountNumber) throws AccountNotFoundException;
}
