package com.bank.app.account_service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountNotFoundException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(AccountNotFoundException.class);

    public AccountNotFoundException(String message) {
        super(message);
        logger.error("AccountNotFoundException: {}", message);
    }
}