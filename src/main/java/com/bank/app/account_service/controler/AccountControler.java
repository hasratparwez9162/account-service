package com.bank.app.account_service.controler;


import com.bank.app.account_service.entity.Account;
import com.bank.app.account_service.exception.AccountNotFoundException;
import com.bank.app.account_service.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/account")
public class AccountControler {
    @Autowired
    private AccountService accountService;

    @PostMapping("/open")
    public ResponseEntity<Account> openAccount(@RequestBody Account account) {
        // Delegate the logic to the service
        Account newAccount = accountService.openAccount(account);
        return new ResponseEntity<>(newAccount, HttpStatus.CREATED);
    }

    @GetMapping("/user-account/{id}")
    public ResponseEntity<List<Account>> getAccountById(@PathVariable Long id) {
        // Delegate the logic to the service
        List<Account> accounts = accountService.getAccountsByUserId(id);
        if (accounts == null || accounts.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }
    @GetMapping("/validate/{accountNumber}")
    public ResponseEntity<String> validateAccount(@PathVariable String accountNumber) {
        try {
            accountService.validateAccountExists(accountNumber);
            return ResponseEntity.ok("Account is valid");
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }



}
