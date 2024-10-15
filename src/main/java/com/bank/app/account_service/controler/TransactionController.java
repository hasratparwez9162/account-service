package com.bank.app.account_service.controler;

import com.bank.app.account_service.entity.Transaction;
import com.bank.app.account_service.exception.AccountNotFoundException;
import com.bank.app.account_service.exception.InsufficientFundsException;
import com.bank.app.account_service.repo.TransactionRepository;
import com.bank.app.account_service.service.AccountService;
import com.bank.core.entity.TransactionRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/account")
@Api(value = "Transaction Management System", tags = "Transaction Controller")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionController(AccountService accountService, TransactionRepository transactionRepository) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
    }

    @PostMapping("/transaction")
    @ApiOperation(value = "Perform a transaction", response = String.class)
    public ResponseEntity<String> performTransaction(@RequestBody TransactionRequest transactionRequest) {
        logger.info("Performing transaction for account: {}", transactionRequest.getAccountNumber());
        try {
            String response = accountService.processTransaction(transactionRequest);
            logger.info("Transaction performed successfully for account: {}", transactionRequest.getAccountNumber());
            return ResponseEntity.ok(response);
        } catch (AccountNotFoundException | InsufficientFundsException | IllegalArgumentException e) {
            logger.error("Error performing transaction for account: {}", transactionRequest.getAccountNumber(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Internal server error", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PostMapping("/transactions")
    @ApiOperation(value = "Perform multiple transactions", response = String.class)
    public ResponseEntity<String> performTransactions(@RequestBody TransactionRequest transactionRequest) {
        logger.info("Performing multiple transactions for account: {}", transactionRequest.getAccountNumber());
        try {
            String response = accountService.processTransactions(transactionRequest);
            logger.info("Transactions performed successfully for account: {}", transactionRequest.getAccountNumber());
            return ResponseEntity.ok(response);
        } catch (AccountNotFoundException | InsufficientFundsException | IllegalArgumentException e) {
            logger.error("Error performing transactions for account: {}", transactionRequest.getAccountNumber(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Internal server error", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/transaction/{accountNumber}")
    @ApiOperation(value = "Get transactions by account number", response = List.class)
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable String accountNumber) {
        logger.info("Fetching transactions for account number: {}", accountNumber);
        List<Transaction> transactions = transactionRepository.findByAccountNumber(accountNumber);
        if (transactions == null || transactions.isEmpty()) {
            logger.warn("No transactions found for account number: {}", accountNumber);
            return ResponseEntity.notFound().build();
        }
        logger.info("Transactions fetched successfully for account number: {}", accountNumber);
        return ResponseEntity.ok(transactions);
    }
}