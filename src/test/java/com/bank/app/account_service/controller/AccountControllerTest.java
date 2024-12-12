package com.bank.app.account_service.controller;

import com.bank.app.account_service.entity.Account;
import com.bank.app.account_service.exception.AccountNotFoundException;
import com.bank.app.account_service.service.AccountService;
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

class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testOpenAccount_Success() {
        Account account = new Account();
        account.setUserId(1L);
        when(accountService.openAccount(any(Account.class))).thenReturn(account);

        ResponseEntity<Account> response = accountController.openAccount(account);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(account, response.getBody());
        verify(accountService, times(1)).openAccount(any(Account.class));
    }

    @Test
    void testOpenAccount_Exception() {
        Account account = new Account();
        account.setUserId(1L);
        when(accountService.openAccount(any(Account.class))).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Account> response = accountController.openAccount(account);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(null, response.getBody());
        verify(accountService, times(1)).openAccount(any(Account.class));
    }

    @Test
    void testGetAccountsByUserId_Success() {
        Long userId = 1L;
        List<Account> accounts = Collections.singletonList(new Account());
        when(accountService.getAccountsByUserId(userId)).thenReturn(accounts);

        ResponseEntity<List<Account>> response = accountController.getAccountsByUserId(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(accounts, response.getBody());
        verify(accountService, times(1)).getAccountsByUserId(userId);
    }

    @Test
    void testGetAccountsByUserId_NotFound() {
        Long userId = 1L;
        when(accountService.getAccountsByUserId(userId)).thenReturn(Collections.emptyList());

        ResponseEntity<List<Account>> response = accountController.getAccountsByUserId(userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(accountService, times(1)).getAccountsByUserId(userId);
    }

    @Test
    void testGetAccountByAccountNumber_Success() {
        String accountNumber = "12345";
        Account account = new Account();
        when(accountService.getAccountByAccountNumber(accountNumber)).thenReturn(account);

        ResponseEntity<Account> response = accountController.getAccountByAccountNumber(accountNumber);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(account, response.getBody());
        verify(accountService, times(1)).getAccountByAccountNumber(accountNumber);
    }

    @Test
    void testGetAccountByAccountNumber_NotFound() {
        String accountNumber = "12345";
        when(accountService.getAccountByAccountNumber(accountNumber)).thenThrow(new AccountNotFoundException("Account not found"));

        ResponseEntity<Account> response = accountController.getAccountByAccountNumber(accountNumber);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(null, response.getBody());
        verify(accountService, times(1)).getAccountByAccountNumber(accountNumber);
    }
}