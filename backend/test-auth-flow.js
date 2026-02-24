const http = require('http');

// Test data
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

// Test registration
async function testRegistration() {
    console.log('=== Test Đăng Ký ===');
    console.log('Email:', testUser.email);
    
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
        const response = await makeRequest(options, testUser);
        
        if (response.statusCode === 200) {
            console.log('✅ Đăng ký thành công!');
            console.log('User ID:', response.body.user.id);
            console.log('Email:', response.body.user.email);
            console.log('Role:', response.body.user.systemRole);
            console.log('Token:', response.body.token.substring(0, 50) + '...');
            return response.body;
        } else {
            console.log('❌ Đăng ký thất bại:', response.body);
            return null;
        }
    } catch (error) {
        console.error('❌ Lỗi đăng ký:', error.message);
        return null;
    }
}

// Test login
async function testLogin(email, password) {
    console.log('\n=== Test Đăng Nhập ===');
    console.log('Email:', email);
    
    const options = {
        hostname: 'localhost',
        port: 8080,
        path: '/api/auth/login',
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    };

    const loginData = { email, password };

    try {
        const response = await makeRequest(options, loginData);
        
        if (response.statusCode === 200) {
            console.log('✅ Đăng nhập thành công!');
            console.log('User ID:', response.body.user.id);
            console.log('Email:', response.body.user.email);
            console.log('Role:', response.body.user.systemRole);
            console.log('Token:', response.body.token.substring(0, 50) + '...');
            return response.body;
        } else {
            console.log('❌ Đăng nhập thất bại:', response.body);
            return null;
        }
    } catch (error) {
        console.error('❌ Lỗi đăng nhập:', error.message);
        return null;
    }
}

// Test login với sai password
async function testInvalidLogin() {
    console.log('\n=== Test Đăng Nhập Sai Password ===');
    
    const options = {
        hostname: 'localhost',
        port: 8080,
        path: '/api/auth/login',
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    };

    const loginData = { 
        email: testUser.email, 
        password: 'WrongPassword!' 
    };

    try {
        const response = await makeRequest(options, loginData);
        
        if (response.statusCode >= 400) {
            console.log('✅ Đăng nhập sai password bị từ chối đúng!');
            console.log('Status:', response.statusCode);
            console.log('Error:', response.body.message);
        } else {
            console.log('❌ Đăng nhập sai password lại thành công?');
        }
    } catch (error) {
        console.error('❌ Lỗi test invalid login:', error.message);
    }
}

// Test login với user không tồn tại
async function testNonExistentUser() {
    console.log('\n=== Test Đăng Nhập User Không Tồn Tại ===');
    
    const options = {
        hostname: 'localhost',
        port: 8080,
        path: '/api/auth/login',
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    };

    const loginData = { 
        email: 'nonexistent@example.com', 
        password: 'Password123!' 
    };

    try {
        const response = await makeRequest(options, loginData);
        
        if (response.statusCode >= 400) {
            console.log('✅ User không tồn tại bị từ chối đúng!');
            console.log('Status:', response.statusCode);
            console.log('Error:', response.body.message);
        } else {
            console.log('❌ User không tồn tại lại đăng nhập được?');
        }
    } catch (error) {
        console.error('❌ Lỗi test non-existent user:', error.message);
    }
}

// Main test flow
async function runAuthTests() {
    console.log('🔐 Chatbot SaaS v2.1 - Authentication Test Suite');
    console.log('================================================\n');
    
    // Test đăng ký
    const registrationResult = await testRegistration();
    
    if (registrationResult) {
        // Test đăng nhập với user vừa đăng ký
        await testLogin(testUser.email, testUser.password);
        
        // Test các trường hợp lỗi
        await testInvalidLogin();
        await testNonExistentUser();
    } else {
        console.log('❌ Không thể test login vì đăng ký thất bại');
    }
    
    console.log('\n=== Test Complete ===');
}

// Check server status
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

// Run tests
(async () => {
    const serverRunning = await checkServer();
    if (!serverRunning) {
        console.log('❌ Server không chạy trên localhost:8080');
        console.log('Khởi động server với: ./gradlew bootRun');
        return;
    }
    
    await runAuthTests();
})();
