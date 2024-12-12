package com.bank.app.account_service.controller;
import com.bank.app.account_service.entity.Transaction;
import com.bank.app.account_service.exception.AccountNotFoundException;
import com.bank.app.account_service.exception.InsufficientFundsException;
import com.bank.app.account_service.repo.TransactionRepository;
import com.bank.app.account_service.service.AccountService;
import com.bank.core.entity.TransactionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionControllerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionController transactionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPerformTransaction_Success() {
        TransactionRequest request = new TransactionRequest();
        request.setFromAccount("12345");
        when(accountService.processTransaction(any(TransactionRequest.class))).thenReturn("Transaction successful");

        ResponseEntity<String> response = transactionController.performTransaction(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Transaction successful", response.getBody());
        verify(accountService, times(1)).processTransaction(any(TransactionRequest.class));
    }

    @Test
    void testPerformTransaction_AccountNotFoundException() {
        TransactionRequest request = new TransactionRequest();
        request.setFromAccount("12345");
        when(accountService.processTransaction(any(TransactionRequest.class))).thenThrow(new AccountNotFoundException("Account not found"));

        ResponseEntity<String> response = transactionController.performTransaction(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Account not found", response.getBody());
        verify(accountService, times(1)).processTransaction(any(TransactionRequest.class));
    }

    @Test
    void testPerformTransaction_InsufficientFundsException() {
        TransactionRequest request = new TransactionRequest();
        request.setFromAccount("12345");
        when(accountService.processTransaction(any(TransactionRequest.class))).thenThrow(new InsufficientFundsException("Insufficient funds"));

        ResponseEntity<String> response = transactionController.performTransaction(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Insufficient funds", response.getBody());
        verify(accountService, times(1)).processTransaction(any(TransactionRequest.class));
    }

    @Test
    void testPerformTransaction_IllegalArgumentException() {
        TransactionRequest request = new TransactionRequest();
        request.setFromAccount("12345");
        when(accountService.processTransaction(any(TransactionRequest.class))).thenThrow(new IllegalArgumentException("Invalid transaction type"));

        ResponseEntity<String> response = transactionController.performTransaction(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid transaction type", response.getBody());
        verify(accountService, times(1)).processTransaction(any(TransactionRequest.class));
    }

    @Test
    void testPerformTransaction_Exception() {
        TransactionRequest request = new TransactionRequest();
        request.setFromAccount("12345");
        when(accountService.processTransaction(any(TransactionRequest.class))).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<String> response = transactionController.performTransaction(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody());
        verify(accountService, times(1)).processTransaction(any(TransactionRequest.class));
    }

    @Test
    void testPerformTransactions_Success() {
        TransactionRequest request = new TransactionRequest();
        request.setFromAccount("12345");
        when(accountService.processTransactions(any(TransactionRequest.class))).thenReturn("Transaction successful");

        ResponseEntity<String> response = transactionController.performTransactions(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Transaction successful", response.getBody());
        verify(accountService, times(1)).processTransactions(any(TransactionRequest.class));
    }

    @Test
    void testPerformTransactions_AccountNotFoundException() {
        TransactionRequest request = new TransactionRequest();
        request.setFromAccount("12345");
        when(accountService.processTransactions(any(TransactionRequest.class))).thenThrow(new AccountNotFoundException("Account not found"));

        ResponseEntity<String> response = transactionController.performTransactions(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Account not found", response.getBody());
        verify(accountService, times(1)).processTransactions(any(TransactionRequest.class));
    }

    @Test
    void testPerformTransactions_InsufficientFundsException() {
        TransactionRequest request = new TransactionRequest();
        request.setFromAccount("12345");
        when(accountService.processTransactions(any(TransactionRequest.class))).thenThrow(new InsufficientFundsException("Insufficient funds"));

        ResponseEntity<String> response = transactionController.performTransactions(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Insufficient funds", response.getBody());
        verify(accountService, times(1)).processTransactions(any(TransactionRequest.class));
    }

    @Test
    void testPerformTransactions_IllegalArgumentException() {
        TransactionRequest request = new TransactionRequest();
        request.setFromAccount("12345");
        when(accountService.processTransactions(any(TransactionRequest.class))).thenThrow(new IllegalArgumentException("Invalid transaction type"));

        ResponseEntity<String> response = transactionController.performTransactions(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid transaction type", response.getBody());
        verify(accountService, times(1)).processTransactions(any(TransactionRequest.class));
    }

    @Test
    void testPerformTransactions_Exception() {
        TransactionRequest request = new TransactionRequest();
        request.setFromAccount("12345");
        when(accountService.processTransactions(any(TransactionRequest.class))).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<String> response = transactionController.performTransactions(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody());
        verify(accountService, times(1)).processTransactions(any(TransactionRequest.class));
    }

    @Test
    void testGetTransactions_Success() {
        String accountNumber = "12345";
        List<Transaction> transactions = Collections.singletonList(new Transaction());
        when(transactionRepository.findByAccountNumber(accountNumber)).thenReturn(transactions);

        ResponseEntity<List<Transaction>> response = transactionController.getTransactions(accountNumber);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(transactions, response.getBody());
        verify(transactionRepository, times(1)).findByAccountNumber(accountNumber);
    }

    @Test
    void testGetTransactions_NotFound() {
        String accountNumber = "12345";
        when(transactionRepository.findByAccountNumber(accountNumber)).thenReturn(Collections.emptyList());

        ResponseEntity<List<Transaction>> response = transactionController.getTransactions(accountNumber);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(transactionRepository, times(1)).findByAccountNumber(accountNumber);
    }
}
