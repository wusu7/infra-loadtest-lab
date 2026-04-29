package com.example.loadtestapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Spring Boot 자동 설정과 컴포넌트 스캔을 켜는 애플리케이션 시작 클래스입니다.
@SpringBootApplication
public class LoadtestApiApplication {
    public static void main(String[] args) {
        // 내장 Tomcat을 띄우고 Controller/Redis/Kafka 설정을 초기화합니다.
        SpringApplication.run(LoadtestApiApplication.class, args);
    }
}
