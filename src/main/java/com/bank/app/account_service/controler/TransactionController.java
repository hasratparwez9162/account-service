package com.bank.app.account_service.controler;
import com.bank.app.account_service.entity.Transaction;
import com.bank.app.account_service.exception.AccountNotFoundException;
import com.bank.app.account_service.exception.InsufficientFundsException;
import com.bank.app.account_service.repo.TransactionRepository;
import com.bank.app.account_service.service.AccountService;
import com.bank.core.entity.TransactionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/account")
public class TransactionController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionRepository transactionRepository;

    @PostMapping("/transaction")
    public ResponseEntity<String> performTransaction(@RequestBody TransactionRequest transactionRequest) {
        try {
            String response = accountService.processTransaction(transactionRequest);
            return ResponseEntity.ok(response);
        } catch (AccountNotFoundException | InsufficientFundsException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // todo.Log the exception
            // todo.logger.error("Internal server error", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/transaction/{accountNumber}")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable String accountNumber) {
        List<Transaction> transactions = transactionRepository.findByAccountNumber(accountNumber);
        return ResponseEntity.ok(transactions);
    }
}

