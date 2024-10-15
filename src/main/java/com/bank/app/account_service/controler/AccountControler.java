package com.bank.app.account_service.controler;

import com.bank.app.account_service.entity.Account;
import com.bank.app.account_service.exception.AccountNotFoundException;
import com.bank.app.account_service.service.AccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/account")
@Api(value = "Account Management System", tags = "Account Controller")
public class AccountControler {

    private static final Logger logger = LoggerFactory.getLogger(AccountControler.class);

    @Autowired
    private AccountService accountService;

    @PostMapping("/open")
    @ApiOperation(value = "Open a new account", response = Account.class)
    public ResponseEntity<Account> openAccount(@RequestBody Account account) {
        logger.info("Opening new account for user: {}", account.getUserName());
        Account newAccount = accountService.openAccount(account);
        logger.info("Account opened successfully with account number: {}", newAccount.getAccountNumber());
        return new ResponseEntity<>(newAccount, HttpStatus.CREATED);
    }

    @GetMapping("/user-account/{id}")
    @ApiOperation(value = "Get accounts by user ID", response = List.class)
    public ResponseEntity<List<Account>> getAccountById(@PathVariable Long id) {
        logger.info("Fetching accounts for user ID: {}", id);
        List<Account> accounts = accountService.getAccountsByUserId(id);
        if (accounts == null || accounts.isEmpty()) {
            logger.warn("No accounts found for user ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        logger.info("Accounts fetched successfully for user ID: {}", id);
        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

    @GetMapping("/validate/{accountNumber}")
    @ApiOperation(value = "Validate an account number", response = String.class)
    public ResponseEntity<String> validateAccount(@PathVariable String accountNumber) {
        logger.info("Validating account number: {}", accountNumber);
        try {
            accountService.validateAccountExists(accountNumber);
            logger.info("Account number {} is valid", accountNumber);
            return ResponseEntity.ok("Account is valid");
        } catch (AccountNotFoundException e) {
            logger.error("Account number {} not found: {}", accountNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}