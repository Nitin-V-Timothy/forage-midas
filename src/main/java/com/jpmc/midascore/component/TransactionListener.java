package com.jpmc.midascore.component;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;

@Component
public class TransactionListener {

    private final DatabaseConduit databaseConduit;
    private final RestTemplate restTemplate;


    public TransactionListener(DatabaseConduit databaseConduit,RestTemplate restTemplate) {
        this.databaseConduit = databaseConduit;
        this.restTemplate = restTemplate;
    }

    @KafkaListener(topics = "${general.kafka-topic}",
            groupId = "midas-core-group")
    public void listen(Transaction transaction) {

        // 1. Fetch users
        UserRecord sender = databaseConduit.findUser(transaction.getSenderId());
        UserRecord recipient = databaseConduit.findUser(transaction.getRecipientId());

        // 2. Validate
        if (sender == null || recipient == null) return;

        if (sender.getBalance() < transaction.getAmount()) return;

        String url = "http://localhost:8080/incentive";

        Incentive incentive =restTemplate.postForObject(url, transaction, Incentive.class);

        float incentiveAmount = incentive != null ? incentive.getAmount() : 0f;

        // 3. Update balances
        sender.setBalance(sender.getBalance() - transaction.getAmount());
        recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentiveAmount);

        // 4. Save transaction
        TransactionRecord record =
                new TransactionRecord(sender, recipient, transaction.getAmount(), incentiveAmount);

        databaseConduit.saveTransaction(record);

        // 5. Persist updated users
        databaseConduit.save(sender);
        databaseConduit.save(recipient);
    }
}