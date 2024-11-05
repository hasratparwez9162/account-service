package com.bank.app.account_service.service;

import com.bank.app.account_service.entity.Account;
import com.bank.app.account_service.exception.AccountNotFoundException;
import com.bank.core.entity.TransactionRequest;

import java.util.List;

public interface AccountService {

    /**
     * Opens a new account.
     * @param account The account details.
     * @return The newly created account.
     */
    Account openAccount(Account account);

    /**
     * Retrieves accounts by user ID.
     * @param userId The ID of the user.
     * @return A list of accounts belonging to the user.
     */
    List<Account> getAccountsByUserId(Long userId);

    /**
     * Processes a transaction.
     * @param transactionRequest The transaction request details.
     * @return A message indicating the result of the transaction.
     */
    String processTransaction(TransactionRequest transactionRequest);

    /**
     * Processes multiple transactions.
     * @param transactionRequest The transaction request details.
     * @return A message indicating the result of the transactions.
     */
    String processTransactions(TransactionRequest transactionRequest);

    /**
     * Validates if an account exists.
     * @param accountNumber The account number to validate.
     */
    void validateAccountExists(String accountNumber);

    /**
     * Retrieves an account by account number.
     * @param accountNumber The account number.
     * @return The account details.
     * @throws AccountNotFoundException if the account is not found.
     */
    Account getAccountByAccountNumber(String accountNumber) throws AccountNotFoundException;
}