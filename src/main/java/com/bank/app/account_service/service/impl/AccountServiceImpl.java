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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountEventProducer accountEventProducer;

    public AccountServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository, AccountEventProducer accountEventProducer) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.accountEventProducer = accountEventProducer;
    }
    @Override
    public Account openAccount(Account account) {
        logger.info("Opening new account for user: {}", account.getUserId());
        account.setAccountNumber(AccountUtil.generateAccountNumber());
        account.setBalance(BigDecimal.ZERO);
        account.setDateOpened(LocalDate.now());
        Account newAccount = accountRepository.save(account);
        accountEventProducer.sendAccountCreatedMessage(newAccount);
        logger.info("Account opened successfully with account number: {}", newAccount.getAccountNumber());
        return newAccount;
    }

    @Override
    public List<Account> getAccountsByUserId(Long userId) {
        logger.info("Fetching accounts for user ID: {}", userId);
        return accountRepository.findByUserId(userId);
    }

    @Transactional
    public String processTransaction(TransactionRequest transactionRequest) {
        String accountNumber = transactionRequest.getAccountNumber();
        logger.info("Processing transaction for account: {}", accountNumber);
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
                if (account.getBalance().compareTo(amount) < 0) {
                    logger.error("Insufficient funds for account: {}", accountNumber);
                    throw new InsufficientFundsException("Insufficient funds");
                }
                updatedBalance = account.getBalance().subtract(amount);
                account.setBalance(updatedBalance);
                break;
            default:
                logger.error("Invalid transaction type: {}", type);
                throw new IllegalArgumentException("Invalid transaction type");
        }

        accountRepository.save(account);
        Transaction transaction = new Transaction(account.getAccountNumber(), type.toUpperCase(), amount, updatedBalance);
        transactionRepository.save(transaction);
        accountEventProducer.sendWithdrawOrCreditBalanceMessage(transaction.getType(), transactionRequest);

        logger.info("Transaction successful for account: {}", accountNumber);
        return "Transaction successful";
    }

    @Transactional
    public String processTransactions(TransactionRequest transactionRequest) {
        String fromAccountNumber = transactionRequest.getFromAccount();
        String toAccountNumber = transactionRequest.getToAccount();
        BigDecimal amount = transactionRequest.getAmount();

        logger.info("Processing transfer of {} from account {} to account {}", amount, fromAccountNumber, toAccountNumber);

        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Invalid from account No: " + fromAccountNumber));

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            logger.error("Insufficient funds in account: {}", fromAccountNumber);
            throw new InsufficientFundsException("Insufficient funds in account: " + fromAccountNumber);
        }

        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Invalid Beneficiary account No: " + toAccountNumber));

        BigDecimal updatedFromBalance = fromAccount.getBalance().subtract(amount);
        fromAccount.setBalance(updatedFromBalance);
        accountRepository.save(fromAccount);

        BigDecimal updatedToBalance = toAccount.getBalance().add(amount);
        toAccount.setBalance(updatedToBalance);
        accountRepository.save(toAccount);

        Transaction withdrawTransaction = new Transaction(fromAccountNumber, "WITHDRAW", amount, updatedFromBalance);
        transactionRepository.save(withdrawTransaction);

        Transaction creditTransaction = new Transaction(toAccountNumber, "CREDIT", amount, updatedToBalance);
        transactionRepository.save(creditTransaction);

        accountEventProducer.sendWithdrawOrCreditBalanceMessage("WITHDRAW", transactionRequest);
        accountEventProducer.sendWithdrawOrCreditBalanceMessage("CREDIT", transactionRequest);

        logger.info("Transfer successful: {} transferred from {} to {}", amount, fromAccountNumber, toAccountNumber);
        return "Transaction successful: " + amount + " transferred from " + fromAccountNumber + " to " + toAccountNumber;
    }

    @Override
    public void validateAccountExists(String accountNumber) {
        logger.info("Validating existence of account: {}", accountNumber);
        if (!accountRepository.existsByAccountNumber(accountNumber)) {
            logger.error("Account not found: {}", accountNumber);
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
    }
    @Override
    public Account getAccountByAccountNumber(String accountNumber) throws AccountNotFoundException {
        logger.info("Fetch of account: {}", accountNumber);
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
    }
}