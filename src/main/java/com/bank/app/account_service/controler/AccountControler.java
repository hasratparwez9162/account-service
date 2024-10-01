package com.bank.app.account_service.controler;


import com.bank.app.account_service.entity.Account;
import com.bank.app.account_service.service.AccountService;
import com.bank.core.entity.TransactionRequest;
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

//    @PostMapping("/transaction")
//    public ResponseEntity<String> performTransaction(@RequestBody TransactionRequest transactionRequest) {
//        String response = accountService.processTransaction(transactionRequest);
//        return ResponseEntity.ok(response);
//    }


}
