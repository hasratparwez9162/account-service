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
        accountEventProducer.sendWithdrawOrCreditBalanceMessage(transaction.getType(),transactionRequest);

        return "Transaction successful";
    }
    @Transactional
    public String processTransactions(TransactionRequest transactionRequest) {
        String fromAccountNumber = transactionRequest.getFromAccount();
        String toAccountNumber = transactionRequest.getToAccount();
        BigDecimal amount = transactionRequest.getAmount();

        // 1. Fetch the source account (fromAccount) for withdrawal
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Invalid from account No: " + fromAccountNumber));

        // 2. Check if the source account has sufficient funds for withdrawal
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in account: " + fromAccountNumber);
        }

        // 3. Fetch the destination account (toAccount) for credit
        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Invalid Beneficiary account No: " + toAccountNumber));

        // 4. Perform withdrawal and update the balance of the source account
        BigDecimal updatedFromBalance = fromAccount.getBalance().subtract(amount);
        fromAccount.setBalance(updatedFromBalance);
        accountRepository.save(fromAccount);  // Save updated balance

        // 5. Perform credit and update the balance of the destination account
        BigDecimal updatedToBalance = toAccount.getBalance().add(amount);
        toAccount.setBalance(updatedToBalance);
        accountRepository.save(toAccount);  // Save updated balance

        // 6. Create and save transaction records for both accounts
        Transaction withdrawTransaction = new Transaction(fromAccountNumber, "WITHDRAW", amount, updatedFromBalance);
        transactionRepository.save(withdrawTransaction);

        Transaction creditTransaction = new Transaction(toAccountNumber, "CREDIT", amount, updatedToBalance);
        transactionRepository.save(creditTransaction);

        // 7. Publish events for both transactions
        accountEventProducer.sendWithdrawOrCreditBalanceMessage("WITHDRAW", transactionRequest);
        accountEventProducer.sendWithdrawOrCreditBalanceMessage("CREDIT", transactionRequest);

        return "Transaction successful: " + amount + " transferred from " + fromAccountNumber + " to " + toAccountNumber;
    }

    @Override
    public void validateAccountExists(String accountNumber) {
        if (!accountRepository.existsByAccountNumber(accountNumber)) {
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
    }
}
