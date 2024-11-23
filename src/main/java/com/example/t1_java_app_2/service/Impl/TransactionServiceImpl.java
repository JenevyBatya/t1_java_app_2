package com.example.t1_java_app_2.service.Impl;

import com.example.t1_java_app_2.config.KafkaConfig;
import com.example.t1_java_app_2.dto.ProcessedTransactionInfo;
import com.example.t1_java_app_2.dto.TransactionInfoDto;
import com.example.t1_java_app_2.dto.enums.TransactionStatus;
import com.example.t1_java_app_2.kafka.KafkaProducer;
import com.example.t1_java_app_2.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {
    private final Map<String, List<TransactionInfoDto>> transactionHistory = new ConcurrentHashMap<>();
    private final KafkaProducer kafkaProducer;

    @Value("${t1.kafka.transaction.time-window}")
    private long timeWindow;
    @Value("${t1.kafka.transaction.max-transactions}")
    private int maxTransactions;

    @Override
    public synchronized void processTransaction(TransactionInfoDto dto) {
        String key = dto.getClientId() + ":" + dto.getAccountId();
        transactionHistory.putIfAbsent(key, new ArrayList<>());
        List<TransactionInfoDto> transactions = transactionHistory.get(key);

        LocalDateTime now = LocalDateTime.now();
        transactions.removeIf(t -> {
            System.out.println(t.getTimestamp());
            Duration duration = Duration.between(t.getTimestamp(), now);
            return duration.toMillis() > timeWindow; // Временное окно в миллисекундах
        });

        transactions.add(dto);
        if (transactions.size() > maxTransactions) {
            updateStatusBlocked(transactions);
            return;
        } else if (dto.getAccountBalance() < dto.getTransactionAmount()) {
            updateStatusRejected(dto);
            return;
        }
        updateStatusAccepted(dto);

    }

    public void clearHistory() {
        transactionHistory.clear();
    }

    private void updateStatusBlocked(List<TransactionInfoDto> transactions) {
        for (TransactionInfoDto transaction : transactions) {
            ProcessedTransactionInfo info = ProcessedTransactionInfo.builder()
                    .transactionId(transaction.getTransactionId())
                    .accountId(transaction.getAccountId())
                    .status(TransactionStatus.BLOCKED)
                    .build();
            kafkaProducer.sendTo("t1_demo_transaction_result", info);
        }
    }

    private void updateStatusRejected(TransactionInfoDto transaction) {
        ProcessedTransactionInfo info = ProcessedTransactionInfo.builder()
                .transactionId(transaction.getTransactionId())
                .accountId(transaction.getAccountId())
                .status(TransactionStatus.REJECTED)
                .build();
        kafkaProducer.sendTo("t1_demo_transaction_result", info);
    }

    private void updateStatusAccepted(TransactionInfoDto transaction) {
        ProcessedTransactionInfo info = ProcessedTransactionInfo.builder()
                .transactionId(transaction.getTransactionId())
                .accountId(transaction.getAccountId())
                .status(TransactionStatus.ACCEPTED)
                .build();
        kafkaProducer.sendTo("t1_demo_transaction_result", info);
    }
}
