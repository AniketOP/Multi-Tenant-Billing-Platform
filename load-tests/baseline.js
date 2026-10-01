import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const PASSWORD = __ENV.SEED_PASSWORD || 'seedpass';
const TENANTS = 5;

export const options = {
    stages: [
        { duration: '10s', target: 10 },   // ramp up
        { duration: '30s', target: 10 },   // hold
        { duration: '5s', target: 0 },     // ramp down
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'],
        'http_req_duration{name:invoices}': ['p(95)<1000'],
        'http_req_duration{name:units}': ['p(95)<1000'],
        'http_req_duration{name:owners}': ['p(95)<1000'],
        'http_req_duration{name:payments}': ['p(95)<1000'],
    },
};

export function setup() {
    const sessions = [];
    for (let t = 1; t <= TENANTS; t++) {
        const res = http.post(
            `${BASE}/auth/login`,
            JSON.stringify({ username: `seed_admin_${t}`, password: PASSWORD }),
            { headers: { 'Content-Type': 'application/json' } }
        );
        check(res, { 'login ok': (r) => r.status === 200 });
        sessions.push({ tenantId: `tenant::seed-${t}`, token: res.json('token') });
    }
    return sessions;
}


export default function (sessions) {
    const s = sessions[(__VU - 1) % sessions.length];
    const base = `${BASE}/tenants/${s.tenantId}`;

    for (const name of ['invoices', 'units', 'owners', 'payments']) {
        const res = http.get(`${base}/${name}`, {
            headers: { Authorization: `Bearer ${s.token}` },
            tags: { name },
        });
        check(res, { [`${name} 200`]: (r) => r.status === 200 });
    }
    sleep(0.5);
}