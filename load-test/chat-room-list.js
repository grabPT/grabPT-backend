import http from 'k6/http';
import { check, sleep } from 'k6';

// 테스트 옵션 설정
export const options = {
    // 가상 유저(VUs) 수와 지속 시간 설정
    stages: [
        { duration: '5s', target: 50 },  // 5초 동안 50명까지 점진적으로 증가
        { duration: '15s', target: 50 }, // 15초 동안 50명 유지
        { duration: '5s', target: 0 },   // 5초 동안 0명으로 감소
    ],
    // 성능 목표 설정 (포트폴리오에 쓰기 좋은 지표)
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95%의 요청이 500ms 이하로 처리되어야 함
        http_req_failed: ['rate<0.01'],   // 에러율이 1% 미만이어야 함
    },
};

// 테스트할 환경 변수 (토큰은 실행할 때 터미널에서 주입하거나 여기에 하드코딩)
const BASE_URL = 'http://localhost:8080';
const TOKEN = __ENV.TOKEN || '여기에_실제_JWT_토큰_입력';

// 가상 유저 1명이 수행할 시나리오
export default function () {
    // 테스트할 API: 채팅방 목록 조회 (안읽음 카운트 포함)
    const url = `${BASE_URL}/chatRoom/list`;

    const params = {
        headers: {
            'Authorization': `Bearer ${TOKEN}`,
            'Content-Type': 'application/json',
        },
    };

    // GET 요청 보내기
    const res = http.get(url, params);

    // 응답 검증 (200 OK 인지 확인)
    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    // 1초 대기 (유저가 새로고침 누르는 텀을 흉내냄)
    sleep(1);
}
