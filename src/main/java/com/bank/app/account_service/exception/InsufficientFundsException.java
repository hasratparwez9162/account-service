package com.bank.app.account_service.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String insufficientFunds) {
        super(insufficientFunds);
    }
}
