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
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Open a new account.
     * @param account The account details.
     * @return A response entity with the newly created account.
     */
    @PostMapping("/open")
    @Operation(summary = "Open a new account", description = "Open a new account", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Account created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Account> openAccount(@RequestBody Account account) {
        logger.info("Opening new account for user: {}", account.getUserId());
        try {
            Account newAccount = accountService.openAccount(account);
            logger.info("Account created successfully with account number: {}", newAccount.getAccountNumber());
            return new ResponseEntity<>(newAccount, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error opening account for user: {}", account.getUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Get accounts by user ID.
     * @param userId The ID of the user.
     * @return A response entity with a list of accounts.
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get accounts by user ID", description = "Get accounts by user ID", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Accounts fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No accounts found")
    })
    public ResponseEntity<List<Account>> getAccountsByUserId(@PathVariable Long userId) {
        logger.info("Fetching accounts for user ID: {}", userId);
        List<Account> accounts = accountService.getAccountsByUserId(userId);
        if (accounts == null || accounts.isEmpty()) {
            logger.warn("No accounts found for user ID: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        logger.info("Accounts fetched successfully for user ID: {}", userId);
        return ResponseEntity.ok(accounts);
    }

    /**
     * Get account by account number.
     * @param accountNumber The account number.
     * @return A response entity with the account details.
     */
    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get account by account number", description = "Get account by account number", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<Account> getAccountByAccountNumber(@PathVariable String accountNumber) {
        logger.info("Fetching account by account number: {}", accountNumber);
        try {
            Account account = accountService.getAccountByAccountNumber(accountNumber);
            logger.info("Account fetched successfully for account number: {}", accountNumber);
            return ResponseEntity.ok(account);
        } catch (AccountNotFoundException e) {
            logger.error("Account not found for account number: {}", accountNumber, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}