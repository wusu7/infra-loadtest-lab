package com.example.loadtestapi;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/kafka")
public class KafkaController {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/publish")
    public Map<String, Object> publish(@RequestParam(defaultValue = "loadtest-topic") String topic) {
        String key = UUID.randomUUID().toString();
        String message = "loadtest-message-" + Instant.now();

        kafkaTemplate.send(topic, key, message);

        return Map.of(
                "topic", topic,
                "key", key,
                "message", message
        );
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("status", "kafka-api-ready");
    }
}
