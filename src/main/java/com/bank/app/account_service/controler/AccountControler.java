package com.bank.app.account_service.controler;

import com.bank.app.account_service.entity.Account;
import com.bank.app.account_service.repo.AccountRepository;
import com.bank.app.account_service.util.AccountUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("account")
public class AccountControler {
    @Autowired
    AccountRepository accountRepository;

    @PostMapping("/open")
    public ResponseEntity<Account> openAcoount(@RequestBody Account account){
        account.setAccountNumber(AccountUtil.generateAccountNumber());
        account.setBalance(BigDecimal.ZERO);
        account.setDateOpened(LocalDate.now());
        //save the account
        Account newAccount = accountRepository.save(account);
        return new ResponseEntity<>(newAccount, HttpStatus.CREATED);
    }
    @GetMapping("/user-account/{id}")  // Ensure this matches the request
    public ResponseEntity<List<Account>> getAccountById(@PathVariable Long id){
        List<Account> getAccountById = accountRepository.findByUserId(id);
        if (getAccountById == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(getAccountById, HttpStatus.OK);
    }

}
