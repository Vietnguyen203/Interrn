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

    // Fetch from as many potential servers as possible
    const servers = ['server-1', 'HCM', 'HN', 'DN', 'Local', 'server-2', 'test'];
    let allUsers = [];
    for (const s of servers) {
        const res = await request('GET', `/users?server=${s}`, null, token);
        if (res.data?.data) {
            allUsers.push(...res.data.data);
        }
    }

    console.log('Total users found across known servers:', allUsers.length);
    console.log('User list (brief):');
    allUsers.forEach(u => {
        console.log(`- ${u.uid} (${u.fullName}) [Server: ${u.server}] [Role: ${u.role}]`);
    });
}

runTests();
