package com.example.loadtestapi;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/redis")
public class RedisController {

    private final StringRedisTemplate redisTemplate;

    public RedisController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/set")
    public Map<String, Object> set(@RequestParam(defaultValue = "loadtest:key") String key,
                                   @RequestParam(defaultValue = "hello") String value) {
        redisTemplate.opsForValue().set(key, value);
        return Map.of("key", key, "value", value);
    }

    @GetMapping("/get")
    public Map<String, Object> get(@RequestParam(defaultValue = "loadtest:key") String key) {
        String value = redisTemplate.opsForValue().get(key);
        return Map.of("key", key, "value", value == null ? "" : value);
    }

    @PostMapping("/incr")
    public Map<String, Object> incr(@RequestParam(defaultValue = "loadtest:counter") String key) {
        Long value = redisTemplate.opsForValue().increment(key);
        return Map.of("key", key, "value", value);
    }
}
