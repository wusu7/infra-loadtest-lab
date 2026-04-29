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

// Kafka에 테스트 메시지를 발행하는 HTTP API입니다.
// k6는 이 Controller를 호출해서 Kafka publish 부하를 만듭니다.
@RestController
@RequestMapping("/kafka")
public class KafkaController {

    // Spring Kafka가 제공하는 메시지 발행 helper입니다.
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // POST /kafka/publish?topic=... 로 지정 topic에 메시지를 발행합니다.
    @PostMapping("/publish")
    public Map<String, Object> publish(@RequestParam(defaultValue = "loadtest-topic") String topic) {
        // 매 요청마다 key를 다르게 만들어 여러 메시지가 구분되게 합니다.
        String key = UUID.randomUUID().toString();
        // 현재 시각을 포함해 발행 시점을 응답에서도 확인할 수 있게 합니다.
        String message = "loadtest-message-" + Instant.now();

        // Kafka broker로 비동기 send를 요청합니다.
        kafkaTemplate.send(topic, key, message);

        return Map.of(
                "topic", topic,
                "key", key,
                "message", message
        );
    }

    // GET /kafka/status 로 Kafka API endpoint가 열려 있는지 확인합니다.
    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("status", "kafka-api-ready");
    }
}
