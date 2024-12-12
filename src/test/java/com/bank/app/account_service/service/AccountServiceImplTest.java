package com.bank.app.account_service.service;

import com.bank.app.account_service.entity.Account;
import com.bank.app.account_service.entity.Transaction;
import com.bank.app.account_service.exception.AccountNotFoundException;
import com.bank.app.account_service.exception.InsufficientFundsException;
import com.bank.app.account_service.repo.AccountRepository;
import com.bank.app.account_service.repo.TransactionRepository;
import com.bank.app.account_service.service.impl.AccountServiceImpl;
import com.bank.core.entity.TransactionRequest;
import com.bank.core.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountEventProducer accountEventProducer;

    @InjectMocks
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test for opening a new account.
     * Validates that the account is created with default values.
     */
    @Test
    void testOpenAccount() {
        // Arrange
        Account account = new Account();
        account.setUserId(1L);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account savedAccount = invocation.getArgument(0);
            savedAccount.setId(1L);
            return savedAccount;
        });

        // Act
        Account createdAccount = accountService.openAccount(account);

        // Assert
        assertNotNull(createdAccount.getId());
        assertEquals(BigDecimal.ZERO, createdAccount.getBalance());
        verify(accountRepository, times(1)).save(any(Account.class));
        verify(accountEventProducer, times(1)).sendAccountCreatedMessage(createdAccount);
    }

    /**
     * Test for retrieving accounts by user ID.
     * Ensures that the method interacts with the repository correctly.
     */
    @Test
    void testGetAccountsByUserId() {
        // Arrange
        Long userId = 1L;

        // Act
        accountService.getAccountsByUserId(userId);

        // Assert
        verify(accountRepository, times(1)).findByUserId(userId);
    }

    /**
     * Test for crediting an account.
     * Ensures the balance is updated and the transaction is saved.
     */
    @Test
    void testProcessCreditTransaction() {
        // Arrange
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("12345");
        request.setType(TransactionType.valueOf("CREDIT"));
        request.setAmount(new BigDecimal("100.00"));

        Account account = new Account();
        account.setAccountNumber("12345");
        account.setBalance(new BigDecimal("200.00"));

        when(accountRepository.findByAccountNumber("12345")).thenReturn(Optional.of(account));

        // Act
        String result = accountService.processTransaction(request);

        // Assert
        assertEquals("Transaction successful", result);
        assertEquals(new BigDecimal("300.00"), account.getBalance());
        verify(accountRepository, times(1)).save(account);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    /**
     * Test for withdrawing from an account with sufficient funds.
     */
    @Test
    void testProcessWithdrawTransaction() {
        // Arrange
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("12345");
        request.setType(TransactionType.valueOf("WITHDRAW"));
        request.setAmount(new BigDecimal("50.00"));

        Account account = new Account();
        account.setAccountNumber("12345");
        account.setBalance(new BigDecimal("200.00"));

        when(accountRepository.findByAccountNumber("12345")).thenReturn(Optional.of(account));

        // Act
        String result = accountService.processTransaction(request);

        // Assert
        assertEquals("Transaction successful", result);
        assertEquals(new BigDecimal("150.00"), account.getBalance());
        verify(accountRepository, times(1)).save(account);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    /**
     * Test for withdrawing from an account with insufficient funds.
     */
    @Test
    void testProcessWithdrawTransaction_InsufficientFunds() {
        // Arrange
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("12345");
        request.setType(TransactionType.valueOf("WITHDRAW"));
        request.setAmount(new BigDecimal("300.00"));

        Account account = new Account();
        account.setAccountNumber("12345");
        account.setBalance(new BigDecimal("200.00"));

        when(accountRepository.findByAccountNumber("12345")).thenReturn(Optional.of(account));

        // Act & Assert
        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class,
                () -> accountService.processTransaction(request));
        assertEquals("Insufficient funds", exception.getMessage());
    }

    /**
     * Test for validating account existence.
     */
    @Test
    void testValidateAccountExists() {
        // Arrange
        String accountNumber = "12345";
        when(accountRepository.existsByAccountNumber(accountNumber)).thenReturn(false);

        // Act & Assert
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class,
                () -> accountService.validateAccountExists(accountNumber));
        assertEquals("Account not found: 12345", exception.getMessage());
    }

    @Test
    void testProcessTransactions_Success() {
        TransactionRequest request = new TransactionRequest();
        request.setFromAccount("12345");
        request.setToAccount("67890");
        request.setAmount(new BigDecimal("100.00"));

        Account fromAccount = new Account();
        fromAccount.setAccountNumber("12345");
        fromAccount.setBalance(new BigDecimal("200.00"));
        fromAccount.setUserName("John Doe");
        fromAccount.setEmail("john.doe@example.com");

        Account toAccount = new Account();
        toAccount.setAccountNumber("67890");
        toAccount.setBalance(new BigDecimal("50.00"));
        toAccount.setUserName("Jane Doe");
        toAccount.setEmail("jane.doe@example.com");

        when(accountRepository.findByAccountNumber("12345")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("67890")).thenReturn(Optional.of(toAccount));

        String result = accountService.processTransactions(request);

        assertEquals("Transaction successful: 100.00 transferred from 12345 to 67890", result);
        verify(accountRepository, times(1)).save(fromAccount);
        verify(accountRepository, times(1)).save(toAccount);
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(accountEventProducer, times(2)).sendWithdrawOrCreditBalanceMessage(anyString(), any(TransactionRequest.class));
    }

    @Test
    void testProcessTransactions_AccountNotFoundException_FromAccount() {
        TransactionRequest request = new TransactionRequest();
        request.setFromAccount("12345");
        request.setToAccount("67890");
        request.setAmount(new BigDecimal("100.00"));

        when(accountRepository.findByAccountNumber("12345")).thenReturn(Optional.empty());

        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            accountService.processTransactions(request);
        });

        assertEquals("Invalid from account No: 12345", exception.getMessage());
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(transactionRepository, times(0)).save(any(Transaction.class));
        verify(accountEventProducer, times(0)).sendWithdrawOrCreditBalanceMessage(anyString(), any(TransactionRequest.class));
    }

    @Test
    void testProcessTransactions_AccountNotFoundException_ToAccount() {
        TransactionRequest request = new TransactionRequest();
        request.setFromAccount("12345");
        request.setToAccount("67890");
        request.setAmount(new BigDecimal("100.00"));

        Account fromAccount = new Account();
        fromAccount.setAccountNumber("12345");
        fromAccount.setBalance(new BigDecimal("200.00"));

        when(accountRepository.findByAccountNumber("12345")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("67890")).thenReturn(Optional.empty());

        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            accountService.processTransactions(request);
        });

        assertEquals("Invalid Beneficiary account No: 67890", exception.getMessage());
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(transactionRepository, times(0)).save(any(Transaction.class));
        verify(accountEventProducer, times(0)).sendWithdrawOrCreditBalanceMessage(anyString(), any(TransactionRequest.class));
    }

    @Test
    void testProcessTransactions_InsufficientFundsException() {
        TransactionRequest request = new TransactionRequest();
        request.setFromAccount("12345");
        request.setToAccount("67890");
        request.setAmount(new BigDecimal("300.00"));

        Account fromAccount = new Account();
        fromAccount.setAccountNumber("12345");
        fromAccount.setBalance(new BigDecimal("200.00"));

        when(accountRepository.findByAccountNumber("12345")).thenReturn(Optional.of(fromAccount));

        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class, () -> {
            accountService.processTransactions(request);
        });

        assertEquals("Insufficient funds in account: 12345", exception.getMessage());
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(transactionRepository, times(0)).save(any(Transaction.class));
        verify(accountEventProducer, times(0)).sendWithdrawOrCreditBalanceMessage(anyString(), any(TransactionRequest.class));
    }
}
