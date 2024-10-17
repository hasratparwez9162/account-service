package com.bank.app.account_service.controler;

import com.bank.app.account_service.entity.Account;
import com.bank.app.account_service.exception.AccountNotFoundException;
import com.bank.app.account_service.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/account")
@Tag(name = "Account Controller", description = "Account Management System")
public class AccountControler {

    private static final Logger logger = LoggerFactory.getLogger(AccountControler.class);

    @Autowired
    private AccountService accountService;

    @PostMapping("/open")
    @Operation(summary = "Open a new account", description = "Open a new account", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Account created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<Account> openAccount(@RequestBody Account account) {
        logger.info("Opening new account for user: {}", account.getUserName());
        Account newAccount = accountService.openAccount(account);
        logger.info("Account opened successfully with account number: {}", newAccount.getAccountNumber());
        return new ResponseEntity<>(newAccount, HttpStatus.CREATED);
    }

    @GetMapping("/user-account/{id}")
    @Operation(summary = "Get accounts by user ID", description = "Get accounts by user ID", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Accounts fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No accounts found")
    })
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
    @Operation(summary = "Validate an account number", description = "Validate an account number", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account is valid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Account not found")
    })
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
    @GetMapping("/details/{accountNumber}")
    @Operation(summary = "Get account details by account number", description = "Get account details by account number", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account details fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<Account> getAccountDetails(@PathVariable String accountNumber) {
        logger.info("Fetching account details for account number: {}", accountNumber);
        try {
            Account account = accountService.getAccountByAccountNumber(accountNumber);
            logger.info("Account details fetched successfully for account number: {}", accountNumber);
            return ResponseEntity.ok(account);
        } catch (AccountNotFoundException e) {
            logger.error("Account number {} not found: {}", accountNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}