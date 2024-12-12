package com.bank.app.account_service.service.impl;

import com.bank.app.account_service.entity.Account;
import com.bank.app.account_service.entity.Transaction;
import com.bank.app.account_service.exception.AccountNotFoundException;
import com.bank.app.account_service.exception.InsufficientFundsException;
import com.bank.app.account_service.repo.AccountRepository;
import com.bank.app.account_service.repo.TransactionRepository;
import com.bank.app.account_service.service.AccountEventProducer;
import com.bank.app.account_service.service.AccountService;
import com.bank.core.entity.TransactionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;


@Service
public class AccountServiceImpl implements AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);
    private static final String WITHDRAW = "WITHDRAW";
    private static final String CREDIT = "CREDIT";
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountEventProducer accountEventProducer;
    SecureRandom random = new SecureRandom();
    public AccountServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository, AccountEventProducer accountEventProducer) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.accountEventProducer = accountEventProducer;
    }

    /**
     * Opens a new account for a user.
     * @param account The account details.
     * @return The newly created account.
     */
    @Override
    public Account openAccount(Account account) {
        logger.info("Opening new account for user: {}", account.getUserId());
        account.setAccountNumber(generateAccountNumber());
        account.setBalance(BigDecimal.ZERO);
        account.setDateOpened(LocalDate.now());
        Account newAccount = accountRepository.save(account);
        accountEventProducer.sendAccountCreatedMessage(newAccount);
        logger.info("Account opened successfully with account number: {}", newAccount.getAccountNumber());
        return newAccount;
    }

    /**
     * Retrieves accounts by user ID.
     * @param userId The ID of the user.
     * @return A list of accounts belonging to the user.
     */
    @Override
    public List<Account> getAccountsByUserId(Long userId) {
        logger.info("Fetching accounts for user ID: {}", userId);
        return accountRepository.findByUserId(userId);
    }

    /**
     * Processes a single transaction (credit or withdraw).
     * @param transactionRequest The transaction request details.
     * @return A message indicating the result of the transaction.
     */
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
            case CREDIT:
                updatedBalance = account.getBalance().add(amount);
                account.setBalance(updatedBalance);
                break;
            case WITHDRAW:
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
        TransactionRequest fromtransaction = new TransactionRequest();
        //copying transactionRequest to fromtransactionRequest all fields
        BeanUtils.copyProperties(transactionRequest, fromtransaction);
        fromtransaction.setAccountNumber(transactionRequest.getAccountNumber());
        fromtransaction.setToAccount(transactionRequest.getAccountNumber());
        fromtransaction.setFromAccount(transactionRequest.getAccountNumber());
        fromtransaction.setBalanceAfterTransaction(updatedBalance);
        fromtransaction.setUserName(account.getUserName());
        fromtransaction.setEmail(account.getEmail());
        accountEventProducer.sendWithdrawOrCreditBalanceMessage(transaction.getType(), fromtransaction);

        logger.info("Transaction successful for account: {}", accountNumber);
        return "Transaction successful";
    }

    /**
     * Processes multiple transactions (transfer between accounts).
     * @param transactionRequest The transaction request details.
     * @return A message indicating the result of the transactions.
     */
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

    Transaction withdrawTransaction = new Transaction(fromAccountNumber, WITHDRAW, amount, updatedFromBalance);
    transactionRepository.save(withdrawTransaction);

    Transaction creditTransaction = new Transaction(toAccountNumber, CREDIT, amount, updatedToBalance);
    transactionRepository.save(creditTransaction);

    // Set userName and email in transactionRequest for event producer
    TransactionRequest fromtransactionRequest = new TransactionRequest();
    //copying transactionRequest to fromtransactionRequest all fields
    BeanUtils.copyProperties(transactionRequest, fromtransactionRequest);
    fromtransactionRequest.setAccountNumber(transactionRequest.getFromAccount());
    fromtransactionRequest.setBalanceAfterTransaction(updatedFromBalance);
    fromtransactionRequest.setUserName(fromAccount.getUserName());
    fromtransactionRequest.setEmail(fromAccount.getEmail());

    TransactionRequest totransactionRequest = new TransactionRequest();
    //copying transactionRequest to totransactionRequest all fields
    BeanUtils.copyProperties(transactionRequest, totransactionRequest);
    totransactionRequest.setAccountNumber(transactionRequest.getToAccount());
    totransactionRequest.setBalanceAfterTransaction(updatedToBalance);
    totransactionRequest.setUserName(toAccount.getUserName());
    totransactionRequest.setEmail(toAccount.getEmail());

    accountEventProducer.sendWithdrawOrCreditBalanceMessage(WITHDRAW, fromtransactionRequest);
    accountEventProducer.sendWithdrawOrCreditBalanceMessage(CREDIT, totransactionRequest);

    logger.info("Transfer successful: {} transferred from {} to {}", amount, fromAccountNumber, toAccountNumber);
    return "Transaction successful: " + amount + " transferred from " + fromAccountNumber + " to " + toAccountNumber;
}

    /**
     * Validates if an account exists by account number.
     * @param accountNumber The account number to validate.
     */
    @Override
    public void validateAccountExists(String accountNumber) {
        logger.info("Validating existence of account: {}", accountNumber);
        if (!accountRepository.existsByAccountNumber(accountNumber)) {
            logger.error("Account not found: {}", accountNumber);
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
    }

    /**
     * Retrieves an account by account number.
     * @param accountNumber The account number.
     * @return The account details.
     * @throws AccountNotFoundException if the account is not found.
     */
    @Override
    public Account getAccountByAccountNumber(String accountNumber) throws AccountNotFoundException {
        logger.info("Fetching account by account number: {}", accountNumber);
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
    }
    private   String generateAccountNumber() {
        // Get the current year
        int year = LocalDate.now().getYear();

        // Generate a random 6-digit number

        int randomDigits = 100000 + random.nextInt(900000); // Ensures it's a 6-digit number

        // Combine the year and the random number to form the account number
        return String.valueOf(year) + randomDigits;
    }
}