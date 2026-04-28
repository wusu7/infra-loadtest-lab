import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 5,
  duration: '30s',
};

const BASE_URL = __ENV.BASE_URL || 'http://172.31.37.123:8080';

export default function () {
  const redisIncr = http.post(`${BASE_URL}/redis/incr`);

  check(redisIncr, {
    'redis incr status is 200': (r) => r.status === 200,
  });

  const kafkaPublish = http.post(`${BASE_URL}/kafka/publish`);

  check(kafkaPublish, {
    'kafka publish status is 200': (r) => r.status === 200,
  });

  sleep(1);
}
