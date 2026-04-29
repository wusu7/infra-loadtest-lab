package com.example.loadtestapi;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// Redis에 값을 쓰고 읽고 증가시키는 HTTP API입니다.
// k6는 Redis에 직접 붙지 않고 이 Controller를 호출합니다.
@RestController
@RequestMapping("/redis")
public class RedisController {

    // 문자열 key/value를 다루는 Spring Redis helper입니다.
    private final StringRedisTemplate redisTemplate;

    public RedisController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // POST /redis/set?key=...&value=... 로 Redis 문자열 값을 저장합니다.
    @PostMapping("/set")
    public Map<String, Object> set(@RequestParam(defaultValue = "loadtest:key") String key,
                                   @RequestParam(defaultValue = "hello") String value) {
        redisTemplate.opsForValue().set(key, value);
        return Map.of("key", key, "value", value);
    }

    // GET /redis/get?key=... 로 Redis 문자열 값을 조회합니다.
    @GetMapping("/get")
    public Map<String, Object> get(@RequestParam(defaultValue = "loadtest:key") String key) {
        String value = redisTemplate.opsForValue().get(key);
        return Map.of("key", key, "value", value == null ? "" : value);
    }

    // POST /redis/incr?key=... 로 Redis counter를 1 증가시킵니다.
    @PostMapping("/incr")
    public Map<String, Object> incr(@RequestParam(defaultValue = "loadtest:counter") String key) {
        Long value = redisTemplate.opsForValue().increment(key);
        return Map.of("key", key, "value", value);
    }
}
