package com.example.loadtestapi;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// k6와 curl로 API 서버가 살아 있는지 간단히 확인하는 Controller입니다.
@RestController
public class HealthController {

    // GET /health 요청에 {"status":"ok"}를 반환합니다.
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "ok");
    }
}
