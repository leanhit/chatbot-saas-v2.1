const http = require('http');

// Test data for registration
const testUser = {
    email: 'testuser' + Date.now() + '@example.com',
    password: 'TestPassword123!'
};

// Function to make HTTP request
function makeRequest(options, data) {
    return new Promise((resolve, reject) => {
        const req = http.request(options, (res) => {
            let body = '';
            res.on('data', (chunk) => {
                body += chunk;
            });
            res.on('end', () => {
                try {
                    const response = {
                        statusCode: res.statusCode,
                        headers: res.headers,
                        body: body ? JSON.parse(body) : null
                    };
                    resolve(response);
                } catch (error) {
                    reject(error);
                }
            });
        });

        req.on('error', (error) => {
            reject(error);
        });

        if (data) {
            req.write(JSON.stringify(data));
        }
        req.end();
    });
}

// Test registration function
async function testRegistration() {
    console.log('=== Test Đăng Ký Người Dùng ===');
    console.log('Testing user registration endpoint...');
    console.log('Test user email:', testUser.email);
    console.log('');

    const options = {
        hostname: 'localhost',
        port: 8080,
        path: '/api/auth/register',
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    };

    try {
        console.log('Sending registration request...');
        const response = await makeRequest(options, testUser);

        console.log('Response Status:', response.statusCode);
        console.log('Response Headers:', JSON.stringify(response.headers, null, 2));
        console.log('Response Body:', JSON.stringify(response.body, null, 2));

        if (response.statusCode === 200) {
            console.log('\n✅ Registration successful!');
            console.log('User ID:', response.body.user.id);
            console.log('User Email:', response.body.user.email);
            console.log('User Role:', response.body.user.role);
            console.log('JWT Token:', response.body.token ? 'Generated successfully' : 'Not found');
        } else {
            console.log('\n❌ Registration failed!');
            console.log('Status Code:', response.statusCode);
            console.log('Error:', response.body);
        }

    } catch (error) {
        console.error('\n❌ Request failed:', error.message);
        console.log('\nPossible reasons:');
        console.log('1. Server is not running on localhost:8080');
        console.log('2. Database connection issues');
        console.log('3. Network connectivity problems');
    }
}

// Test duplicate registration
async function testDuplicateRegistration() {
    console.log('\n=== Test Đăng Ký Trùng Email ===');
    console.log('Testing duplicate email registration...');

    const options = {
        hostname: 'localhost',
        port: 8080,
        path: '/api/auth/register',
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    };

    try {
        console.log('Sending duplicate registration request...');
        const response = await makeRequest(options, testUser);

        console.log('Response Status:', response.statusCode);
        console.log('Response Body:', JSON.stringify(response.body, null, 2));

        if (response.statusCode >= 400) {
            console.log('\n✅ Duplicate registration correctly rejected!');
        } else {
            console.log('\n❌ Duplicate registration should have been rejected!');
        }

    } catch (error) {
        console.error('\n❌ Request failed:', error.message);
    }
}

// Run tests
async function runTests() {
    console.log('Chatbot SaaS v2.1 - Registration Test');
    console.log('=====================================');
    
    await testRegistration();
    await testDuplicateRegistration();
    
    console.log('\n=== Test Complete ===');
}

// Check if server is running first
async function checkServer() {
    try {
        const options = {
            hostname: 'localhost',
            port: 8080,
            path: '/api/actuator/health',
            method: 'GET'
        };
        
        await makeRequest(options);
        return true;
    } catch (error) {
        return false;
    }
}

// Main execution
(async () => {
    const serverRunning = await checkServer();
    if (!serverRunning) {
        console.log('❌ Server is not running on localhost:8080');
        console.log('Please start the application first:');
        console.log('  ./gradlew bootRun');
        console.log('Or check if the server is running on a different port');
        return;
    }
    
    await runTests();
})();
