package com.bank.app.account_service.service.impl;


import com.bank.app.account_service.entity.Account;
import com.bank.app.account_service.entity.Transaction;
import com.bank.app.account_service.exception.AccountNotFoundException;
import com.bank.app.account_service.exception.InsufficientFundsException;
import com.bank.app.account_service.repo.AccountRepository;
import com.bank.app.account_service.repo.TransactionRepository;
import com.bank.app.account_service.service.AccountEventProducer;
import com.bank.app.account_service.service.AccountService;
import com.bank.app.account_service.util.AccountUtil;
import com.bank.core.entity.TransactionRequest;
import com.bank.core.entity.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;


   @Autowired
    AccountEventProducer accountEventProducer;



    @Override
    public Account openAccount(Account account) {
        account.setAccountNumber(AccountUtil.generateAccountNumber());
        account.setBalance(BigDecimal.ZERO);
        account.setDateOpened(LocalDate.now());
        Account newAccount = accountRepository.save(account);
        accountEventProducer.sendAccountCreatedMessage(newAccount);
        return newAccount;
    }

    @Override
    public List<Account> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    @Transactional
    public String processTransaction(TransactionRequest transactionRequest) {
        String accountNumber = transactionRequest.getAccountNumber();
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        String type = String.valueOf(transactionRequest.getType());
        BigDecimal amount = transactionRequest.getAmount();
        BigDecimal updatedBalance;

        switch (type.toUpperCase()) {
            case "CREDIT":
                updatedBalance = account.getBalance().add(amount);
                account.setBalance(updatedBalance);
                break;
            case "WITHDRAW":
                if (account.getBalance().compareTo(amount) < 0) {  // compareTo to check balance
                    throw new InsufficientFundsException("Insufficient funds");
                }
                updatedBalance = account.getBalance().subtract(amount); // Using subtract for BigDecimal
                account.setBalance(updatedBalance);
                break;
            default:
                throw new IllegalArgumentException("Invalid transaction type");
        }

        // Save the updated account balance
        accountRepository.save(account);

        // Create and save the transaction record
        Transaction transaction = new Transaction(account.getAccountNumber(), type.toUpperCase(), amount, updatedBalance);
        transactionRepository.save(transaction);

        return "Transaction successful";
    }
}
