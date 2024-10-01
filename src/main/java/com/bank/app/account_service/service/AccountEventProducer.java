package com.bank.app.account_service.service;

import com.bank.app.account_service.entity.Account;
import com.bank.core.entity.AccountNotification;
import com.bank.core.entity.TransactionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AccountEventProducer {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    private static final String ACCOUNT_TOPIC = "account-service-topic";
    private static final String TX_TOPIC = "transaction-service-topic";

    public void sendAccountCreatedMessage(Account account){
        AccountNotification accountNotification = new AccountNotification(account.getAccountNumber(),account.getBalance(),account.getDateOpened(),account.getUserId(),account.getUserName(),account.getEmail(),account.getPhoneNumber());
        kafkaTemplate.send(ACCOUNT_TOPIC,"Account Created", accountNotification);
    }
    public void  sendWithdrawOrCreditBalanceMessage(String key , TransactionRequest transactionRequest){
        kafkaTemplate.send(TX_TOPIC,key,transactionRequest);
    }


}
