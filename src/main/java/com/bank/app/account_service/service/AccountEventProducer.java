package com.bank.app.account_service.service;

import com.bank.app.account_service.entity.Account;
import com.bank.core.entity.AccountNotification;
import com.bank.core.entity.TransactionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AccountEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(AccountEventProducer.class);
    private static final String ACCOUNT_TOPIC = "account-service-topic";
    private static final String TX_TOPIC = "transaction-service-topic";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Sends a message indicating that an account has been created.
     * @param account The account that was created.
     */
    public void sendAccountCreatedMessage(Account account) {
        AccountNotification accountNotification = new AccountNotification(account.getAccountNumber(), account.getBalance(), account.getDateOpened(), account.getUserId(), account.getUserName(), account.getEmail(), account.getPhoneNumber());
        logger.info("Sending account created message for account number: {}", account.getAccountNumber());
        kafkaTemplate.send(ACCOUNT_TOPIC, "Open", accountNotification);
        logger.info("Account created message sent successfully for account number: {}", account.getAccountNumber());
    }

    /**
     * Sends a message indicating a withdrawal or credit transaction.
     * @param key The transaction type (withdraw or credit).
     * @param transactionRequest The transaction request details.
     */
    public void sendWithdrawOrCreditBalanceMessage(String key, TransactionRequest transactionRequest) {
        logger.info("Sending {} message for account number: {}", key, transactionRequest.getAccountNumber());
        kafkaTemplate.send(TX_TOPIC, key, transactionRequest);
        logger.info("{} message sent successfully for account number: {}", key, transactionRequest.getAccountNumber());
    }
}