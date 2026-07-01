import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '10s', target: 50 }, // 10초 동안 동시 접속(연결) 50개로 증가
        { duration: '10s', target: 50 }, // 10초 동안 유지 (서버 뚜들겨 패기)
        { duration: '5s', target: 0 },   // 5초 동안 서서히 종료
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95%의 응답이 500ms 이내여야 함
    },
};

export default function () {
    // 💡 roomId를 1번으로 수정했습니다!
    const url = 'http://localhost:8080/chatRoom/1/messages?cursor=0';
    const token = __ENV.TOKEN;

    const params = {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    };

    const res = http.get(url, params);

    check(res, {
        'is status 200': (r) => r.status === 200,
    });

    sleep(1);
}
