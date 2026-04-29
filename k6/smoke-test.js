import http from 'k6/http';
import { check, sleep } from 'k6';

// 가장 작은 부하로 k6 실행 환경 자체가 정상인지 확인하는 smoke test입니다.
export const options = {
  vus: 1,
  duration: '10s',
};

export default function () {
  // k6 공식 테스트 사이트에 GET 요청을 보냅니다.
  const res = http.get('https://test.k6.io');

  // HTTP 200이면 k6 실행과 네트워크 접근이 정상이라고 판단합니다.
  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  // 너무 촘촘하게 요청하지 않도록 각 반복 사이에 1초 쉽니다.
  sleep(1);
}
