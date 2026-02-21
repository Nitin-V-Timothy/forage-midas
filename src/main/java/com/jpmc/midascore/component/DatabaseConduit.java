package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.stereotype.Component;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.repository.TransactionRecordRepository;

@Component
public class DatabaseConduit {
    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRepository;

    public DatabaseConduit(UserRepository userRepository,TransactionRecordRepository transactionRepository) 
    {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    public void save(UserRecord userRecord) {
        userRepository.save(userRecord);
    }
    public UserRecord findUser(long id) 
    {
        return userRepository.findById(id);
    }
    public void saveTransaction(TransactionRecord transaction) 
    {
        transactionRepository.save(transaction);
    }
}
