package com.example.t1_java_app_2.kafka;

import com.example.t1_java_app_2.util.ParserJson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor

public class KafkaProducer {
    private final KafkaTemplate<String, String> template;

    public void sendTo(String topic, Object payload) {
        try {
            template.send(topic, UUID.randomUUID().toString(), ParserJson.toJson(payload)).get();
        } catch (Exception ex) {
            log.error("Failed to send message to topic {}: {}", topic, ex.getMessage(), ex);
        } finally {
            template.flush();
        }
        log.info("Message sent to topic {} with payload {}", topic, payload);

    }
}
