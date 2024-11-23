package com.example.t1_java_app_2.kafka;

import com.example.t1_java_app_2.dto.TransactionInfoDto;
import com.example.t1_java_app_2.service.TransactionService;
import com.example.t1_java_app_2.util.ParserJson;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaTransactionAcceptConsumer {
    private final TransactionService transactionService;

    @KafkaListener(id = "${t1.kafka.consumer.group-id-transaction-info}",
            topics = "t1_demo_transaction_accept",
            containerFactory = "kafkaStringContainerFactory")
    public void listenToTransactionInfo(@Payload List<String> messageList,
                                        Acknowledgment ack,
                                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                        @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        for (String info : messageList) {
            try {
                transactionService.processTransaction(ParserJson.fromJson(info, TransactionInfoDto.class));
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
            }finally {
                log.error("After finaly");
                ack.acknowledge();
                log.error("After first ack");
            }

        }
    }

}
