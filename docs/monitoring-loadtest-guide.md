# Infra Load Test Lab 설명서

이 저장소는 Jenkins에서 k6 부하테스트를 실행하고, Target 서버의 Spring Boot API가 Redis와 Kafka 작업을 대신 수행하게 만든 실습 프로젝트입니다. Redis와 Kafka를 테스트 도구에 직접 노출하지 않고 HTTP API를 중간 계층으로 둔 것이 핵심입니다.

## 전체 구조

```text
Jenkins
  -> k6
  -> Spring Boot Test API
  -> Redis / Kafka
  -> Exporters
  -> Prometheus
  -> Grafana
```

Jenkins는 `Jenkinsfile`에 정의된 파이프라인을 실행합니다. 파이프라인은 k6 설치 여부를 확인하고, smoke test와 Redis/Kafka API 부하테스트를 차례로 실행한 뒤 결과 JSON을 Jenkins artifact로 저장합니다.

Target 서버의 `docker-compose.yml`은 Redis, Kafka, Spring Boot API, Prometheus, Grafana를 함께 실행합니다. Prometheus는 Spring Boot Actuator와 Redis/Kafka exporter에서 메트릭을 수집하고, Grafana는 provisioning 설정으로 Prometheus data source와 대시보드를 자동 로드합니다.

## 주요 파일

`Jenkinsfile`은 CI 파이프라인입니다. `Run k6 Smoke Test`는 k6 자체가 정상인지 확인하고, `Run Redis Kafka API Load Test`는 Target 서버의 Spring Boot API를 호출해 Redis 증가 연산과 Kafka publish를 검증합니다.

`k6/smoke-test.js`는 아주 작은 테스트입니다. 외부 샘플 사이트에 요청을 보내 k6 실행 환경과 네트워크를 확인합니다.

`k6/api-load-test.js`는 실제 실습용 부하테스트입니다. `/redis/incr`와 `/kafka/publish`를 반복 호출하면서 Spring Boot API, Redis, Kafka 흐름을 함께 검증합니다.

`test-api`는 Spring Boot API입니다. `/health`, `/redis/*`, `/kafka/*`, `/actuator/prometheus` endpoint를 제공합니다.

`prometheus/prometheus.yml`은 Prometheus scrape 설정입니다. Spring Boot, Redis exporter, Kafka exporter를 5초마다 수집합니다.

`grafana/provisioning/datasources/prometheus.yml`은 Grafana가 시작될 때 Prometheus data source를 자동 등록합니다. `grafana/provisioning/dashboards/dashboards.yml`은 `grafana/dashboards/*.json` 파일을 `Load Test` 폴더의 대시보드로 자동 로드합니다.

`docker-compose.yml`은 Target 서버에서 실행할 전체 컨테이너 구성을 담고 있습니다.

## Spring Boot API

Spring Boot API는 부하테스트의 HTTP 진입점입니다.

```text
GET  /health
POST /redis/set
GET  /redis/get
POST /redis/incr
POST /kafka/publish
GET  /kafka/status
GET  /actuator/prometheus
```

`RedisController`는 `StringRedisTemplate`으로 Redis에 접근합니다. k6는 `/redis/incr`를 호출해서 counter 증가 부하를 만듭니다.

`KafkaController`는 `KafkaTemplate`으로 Kafka에 메시지를 발행합니다. k6는 `/kafka/publish`를 호출해서 publish 흐름을 검증합니다.

`application.yml`의 `management` 설정은 Actuator endpoint를 열어 Prometheus가 `/actuator/prometheus`를 수집할 수 있게 합니다.

## 모니터링

Prometheus target은 3개입니다.

```text
spring-boot-test-api -> test-api:8080/actuator/prometheus
redis-exporter      -> redis-exporter:9121/metrics
kafka-exporter      -> kafka-exporter:9308/metrics
```

Grafana는 `http://<Target Public IP>:3000`으로 접속합니다. 초기 계정은 `admin/admin`입니다. Prometheus data source URL은 Docker Compose 내부 서비스 이름을 사용해 `http://prometheus:9090`으로 자동 설정됩니다.

## 확인 명령

Target 서버에서 컨테이너를 실행합니다.

```bash
docker compose up -d --build
docker ps
```

API와 exporter를 확인합니다.

```bash
curl http://localhost:8080/health
curl http://localhost:8080/actuator/prometheus
curl http://localhost:9121/metrics
curl http://localhost:9308/metrics
curl http://localhost:9090/-/ready
```

Grafana 접속 후 `Load Test` 폴더에서 자동 등록된 대시보드를 확인합니다.

```text
Spring Boot Load Test
Redis Load Test
Kafka Load Test
k6 Load Test
```

## Grafana에서 먼저 볼 PromQL

Spring Boot 요청 수:

```promql
sum(rate(http_server_requests_seconds_count[1m]))
```

Spring Boot p95 응답 시간:

```promql
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[1m])) by (le))
```

Redis ops/sec:

```promql
rate(redis_commands_processed_total[1m])
```

Kafka broker 수:

```promql
kafka_brokers
```

## 성공 기준

Jenkins에서는 smoke test와 API load test가 모두 성공하고 아래 artifact가 저장되면 됩니다.

```text
results/smoke-summary.json
results/api-load-summary.json
```

Prometheus에서는 `/targets` 화면에서 아래 3개가 모두 `UP`이어야 합니다.

```text
spring-boot-test-api
redis-exporter
kafka-exporter
```

Grafana에서는 Prometheus data source가 자동 등록되고, `Load Test` 폴더의 Spring Boot / Redis / Kafka 패널에서 값이 보이면 모니터링 구조가 완성된 상태입니다. `k6 Load Test` 대시보드는 k6 Prometheus remote write 출력을 사용할 때 값이 표시됩니다.
