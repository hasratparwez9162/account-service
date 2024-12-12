package com.bank.app.account_service.entity;



import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;

    private String type; // "DEPOSIT", "WITHDRAWAL"
    private BigDecimal amount;
    private BigDecimal balanceAfterTransaction;
    private LocalDateTime timestamp;

    public Transaction() {}

    public Transaction(String accountNumber, String type, BigDecimal amount, BigDecimal balanceAfterTransaction) {
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfterTransaction = balanceAfterTransaction;
        this.timestamp = LocalDateTime.now();
    }


}
