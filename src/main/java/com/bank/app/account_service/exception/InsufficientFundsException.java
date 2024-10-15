package com.bank.app.account_service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsufficientFundsException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(InsufficientFundsException.class);

    public InsufficientFundsException(String message) {
        super(message);
        logger.error("InsufficientFundsException: {}", message);
    }
}
