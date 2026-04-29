import http from 'k6/http';
import { check, sleep } from 'k6';

// 5명의 가상 사용자가 30초 동안 API를 반복 호출합니다.
export const options = {
  vus: 5,
  duration: '30s',
};

// Jenkins에서 -e BASE_URL=... 로 덮어쓸 수 있고, 없으면 Target 서버 API 주소를 사용합니다.
const BASE_URL = __ENV.BASE_URL || 'http://172.31.37.123:8080';

export default function () {
  // Spring Boot API를 통해 Redis counter를 1 증가시킵니다.
  const redisIncr = http.post(`${BASE_URL}/redis/incr`);

  // Redis API가 정상 응답했는지 확인합니다.
  check(redisIncr, {
    'redis incr status is 200': (r) => r.status === 200,
  });

  // Spring Boot API를 통해 Kafka topic으로 메시지를 publish합니다.
  const kafkaPublish = http.post(`${BASE_URL}/kafka/publish`);

  // Kafka publish API가 정상 응답했는지 확인합니다.
  check(kafkaPublish, {
    'kafka publish status is 200': (r) => r.status === 200,
  });

  // 반복 간격을 둬서 짧은 테스트에서도 요청 흐름을 보기 쉽게 만듭니다.
  sleep(1);
}
