package com.example.t1_java_app_2.service.Impl;

import com.example.t1_java_app_2.config.KafkaConfig;
import com.example.t1_java_app_2.dto.ProcessedTransactionInfo;
import com.example.t1_java_app_2.dto.TransactionInfoDto;
import com.example.t1_java_app_2.dto.enums.TransactionStatus;
import com.example.t1_java_app_2.kafka.KafkaProducer;
import com.example.t1_java_app_2.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the {@link TransactionService} interface for processing transactions.
 * <p>
 * This service validates transactions, updates their status, and sends results
 * to a Kafka topic using the {@link KafkaProducer}.
 * </p>
 * <p>
 * Maintains a history of recent transactions per client and account, with checks
 * for a time window and transaction limits.
 * </p>
 */
@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {
    private final Map<String, List<TransactionInfoDto>> transactionHistory = new ConcurrentHashMap<>();
    private final KafkaProducer kafkaProducer;

    /**
     * The time window for filtering transactions, in milliseconds.
     */
    @Value("${t1.kafka.transaction.time-window}")
    private long timeWindow;

    /**
     * The maximum number of allowed transactions within the time window.
     */
    @Value("${t1.kafka.transaction.max-transactions}")
    private int maxTransactions;

    /**
     * Processes a transaction by validating its status based on the transaction history
     * and business rules.
     * <ul>
     *     <li>If the number of transactions exceeds the limit, the status is set to BLOCKED.</li>
     *     <li>If the account balance is insufficient for the transaction amount, the status is set to REJECTED.</li>
     *     <li>Otherwise, the status is set to ACCEPTED.</li>
     * </ul>
     *
     * @param dto the transaction data to process, as {@link TransactionInfoDto}.
     */
    @Override
    public synchronized void processTransaction(TransactionInfoDto dto) {
        String key = dto.getClientId() + ":" + dto.getAccountId();
        transactionHistory.putIfAbsent(key, new ArrayList<>());
        List<TransactionInfoDto> transactions = transactionHistory.get(key);

        LocalDateTime now = LocalDateTime.now();
        transactions.removeIf(t -> {
            Duration duration = Duration.between(t.getTimestamp(), now);
            return duration.toMillis() > timeWindow; // Removes expired transactions
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

    /**
     * Clears the entire transaction history.
     */
    public void clearHistory() {
        transactionHistory.clear();
    }

    /**
     * Updates the status of a list of transactions to BLOCKED and sends the results to Kafka.
     *
     * @param transactions the list of transactions to update.
     */
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

    /**
     * Updates the status of a single transaction to REJECTED and sends the result to Kafka.
     *
     * @param transaction the transaction to update.
     */
    private void updateStatusRejected(TransactionInfoDto transaction) {
        ProcessedTransactionInfo info = ProcessedTransactionInfo.builder()
                .transactionId(transaction.getTransactionId())
                .accountId(transaction.getAccountId())
                .status(TransactionStatus.REJECTED)
                .build();
        kafkaProducer.sendTo("t1_demo_transaction_result", info);
    }

    /**
     * Updates the status of a single transaction to ACCEPTED and sends the result to Kafka.
     *
     * @param transaction the transaction to update.
     */
    private void updateStatusAccepted(TransactionInfoDto transaction) {
        ProcessedTransactionInfo info = ProcessedTransactionInfo.builder()
                .transactionId(transaction.getTransactionId())
                .accountId(transaction.getAccountId())
                .status(TransactionStatus.ACCEPTED)
                .build();
        kafkaProducer.sendTo("t1_demo_transaction_result", info);
    }
}
