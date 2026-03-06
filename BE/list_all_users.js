const http = require('http');

async function request(method, path, body, token) {
    return new Promise((resolve, reject) => {
        const bodyString = body ? JSON.stringify(body) : '';
        const options = {
            hostname: 'localhost',
            port: 9720,
            path: `/foodordersystem/api${path}`,
            method: method,
            headers: {
                'Content-Type': 'application/json',
                ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
                ...(bodyString ? { 'Content-Length': bodyString.length } : {})
            }
        };

        const req = http.request(options, (res) => {
            let data = '';
            res.on('data', (chunk) => { data += chunk; });
            res.on('end', () => {
                try {
                    resolve({ status: res.statusCode, data: data ? JSON.parse(data) : null });
                } catch (e) {
                    resolve({ status: res.statusCode, body: data });
                }
            });
        });

        req.on('error', (e) => reject(e));
        if (bodyString) req.write(bodyString);
        req.end();
    });
}

async function runTests() {
    const loginRes = await request('POST', '/users/login', {
        username: 'admin',
        password: 'admin',
        server: 'server-1'
    });
    const token = loginRes.data.data.token;

    const servers = ['server-1', 'HCM', 'HN', 'DN', 'Local'];
    for (const s of servers) {
        console.log(`\n--- Staff for ${s} ---`);
        const res = await request('GET', `/users?server=${s}`, null, token);
        console.log(`Count: ${res.data?.data?.length || 0}`);
        if (res.data?.data?.length > 0) {
            console.log(JSON.stringify(res.data.data, null, 2));
        }
    }
}

runTests();
