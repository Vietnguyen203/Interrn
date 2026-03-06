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
                    resolve({ status: res.statusCode, data: data ? JSON.parse(data) : null, body: data });
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
    console.log('--- Logging in ---');
    const loginRes = await request('POST', '/users/login', {
        username: 'admin',
        password: 'admin',
        server: 'server-1'
    });
    if (!loginRes.data?.data?.token) {
        console.error('Login failed');
        return;
    }
    const token = loginRes.data.data.token;

    const testUser = `test_${Date.now()}`;
    console.log(`\n--- Registering ${testUser} ---`);
    const regRes = await request('POST', '/users/register', {
        username: testUser,
        password: 'password123',
        fullName: 'Test User Agent',
        role: 'WAITER',
        server: 'server-1',
        employeeId: testUser,
        email: `${testUser}@example.com`
    }, token);
    console.log('Status:', regRes.status);
    console.log('Response:', regRes.body);

    console.log('\n--- Fetching Staff for server-1 ---');
    const staffRes = await request('GET', '/users?server=server-1', null, token);
    console.log('Status:', staffRes.status);
    const users = staffRes.data?.data || [];
    const found = users.find(u => u.uid === testUser);
    if (found) {
        console.log('SUCCESS: New user found in list!');
        console.log(JSON.stringify(found, null, 2));
    } else {
        console.log('FAILURE: New user NOT found in list.');
        console.log('Full list count:', users.length);
        console.log('User IDs in list:', users.map(u => u.uid).join(', '));
    }
}

runTests();
