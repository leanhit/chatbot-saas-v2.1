const axios = require('axios');

// Configuration
const BASE_URL = 'http://localhost:8080/api';
let authToken = '';

// Helper functions
const api = axios.create({
    baseURL: BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    }
});

// Test data
const testUser = {
    email: 'license-test@example.com',
    password: 'TestPassword123!',
    confirmPassword: 'TestPassword123!'
};

const testLicense = {
    userId: null, // Will be set after user creation
    planName: 'Basic Plan',
    isActive: true,
    expiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString(), // 30 days from now
    features: ['facebook', 'zalo'],
    modules: ['reengage', 'ai-reply'],
    limits: {
        bots: 2,
        storage: 1000
    }
};

async function registerUser() {
    console.log('🔧 Registering test user...');
    try {
        const response = await api.post('/auth/register', testUser);
        console.log('✅ User registered successfully');
        console.log('User ID:', response.data.id);
        testLicense.userId = response.data.id;
        return response.data;
    } catch (error) {
        if (error.response?.status === 409) {
            console.log('ℹ️ User already exists, trying to login...');
            return await loginUser();
        }
        console.error('❌ Registration failed:', error.response?.data || error.message);
        throw error;
    }
}

async function loginUser() {
    console.log('🔐 Logging in...');
    try {
        const response = await api.post('/auth/login', {
            email: testUser.email,
            password: testUser.password
        });
        console.log('✅ Login successful');
        authToken = response.data.token;
        testLicense.userId = response.data.id;
        return response.data;
    } catch (error) {
        console.error('❌ Login failed:', error.response?.data || error.message);
        throw error;
    }
}

async function testGetLicenseMe() {
    console.log('📋 Testing GET /license/me...');
    try {
        const response = await api.get('/license/me', {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });
        console.log('✅ GET /license/me successful');
        console.log('License data:', JSON.stringify(response.data, null, 2));
        return response.data;
    } catch (error) {
        if (error.response?.status === 404) {
            console.log('ℹ️ No license found (expected for new user)');
            return null;
        }
        console.error('❌ GET /license/me failed:', error.response?.data || error.message);
        throw error;
    }
}

async function testCreateLicense() {
    console.log('🔧 Creating license...');
    try {
        const response = await api.post('/license', testLicense, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });
        console.log('✅ License created successfully');
        console.log('License ID:', response.data.id);
        return response.data;
    } catch (error) {
        console.error('❌ License creation failed:', error.response?.data || error.message);
        throw error;
    }
}

async function testLicenseAfterCreation() {
    console.log('📋 Testing license after creation...');
    try {
        const response = await api.get('/license/me', {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });
        console.log('✅ License retrieved successfully');
        console.log('License data:', JSON.stringify(response.data, null, 2));
        
        // Test JWT compatibility fields
        console.log('🔑 JWT Fields:');
        console.log('- exp (timestamp):', response.data.exp);
        console.log('- sub (user ID):', response.data.sub);
        console.log('- email:', response.data.email);
        console.log('- features:', response.data.features);
        console.log('- modules:', response.data.modules);
        console.log('- limits:', response.data.limits);
        
        return response.data;
    } catch (error) {
        console.error('❌ License retrieval failed:', error.response?.data || error.message);
        throw error;
    }
}

async function testFeatureCheck() {
    console.log('🔍 Testing feature access check...');
    try {
        const response = await api.get('/license/check/feature/facebook', {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });
        console.log('✅ Feature check successful');
        console.log('Has facebook feature:', response.data.data);
        return response.data;
    } catch (error) {
        console.error('❌ Feature check failed:', error.response?.data || error.message);
        throw error;
    }
}

async function testModuleCheck() {
    console.log('🔍 Testing module access check...');
    try {
        const response = await api.get('/license/check/module/ai-reply', {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });
        console.log('✅ Module check successful');
        console.log('Has ai-reply module:', response.data.data);
        return response.data;
    } catch (error) {
        console.error('❌ Module check failed:', error.response?.data || error.message);
        throw error;
    }
}

async function testLimitCheck() {
    console.log('🔍 Testing limit check...');
    try {
        const response = await api.get('/license/check/limit/bots', {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });
        console.log('✅ Limit check successful');
        console.log('Bots limit:', response.data.data);
        return response.data;
    } catch (error) {
        console.error('❌ Limit check failed:', error.response?.data || error.message);
        throw error;
    }
}

async function testRevokeLicense() {
    console.log('🚫 Testing license revocation...');
    try {
        const license = await testGetLicenseMe();
        if (!license) {
            console.log('ℹ️ No license to revoke');
            return;
        }
        
        const response = await api.delete(`/license/${license.id}`, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });
        console.log('✅ License revoked successfully');
        console.log('Response:', response.data);
        return response.data;
    } catch (error) {
        console.error('❌ License revocation failed:', error.response?.data || error.message);
        throw error;
    }
}

async function runAllTests() {
    console.log('🚀 Starting License API Tests\n');
    
    try {
        // Setup
        await registerUser();
        
        // Test 1: Get license before creation (should return 404)
        await testGetLicenseMe();
        console.log('');
        
        // Test 2: Create license (admin only - might fail without admin role)
        try {
            await testCreateLicense();
        } catch (error) {
            if (error.response?.status === 403) {
                console.log('⚠️ Cannot create license (admin rights required). Skipping creation test.');
                console.log('💡 To test license creation, run this with an admin account or update user role.');
            } else {
                throw error;
            }
        }
        console.log('');
        
        // Test 3: Get license after creation
        await testLicenseAfterCreation();
        console.log('');
        
        // Test 4: Feature access check
        await testFeatureCheck();
        console.log('');
        
        // Test 5: Module access check
        await testModuleCheck();
        console.log('');
        
        // Test 6: Limit check
        await testLimitCheck();
        console.log('');
        
        // Test 7: Revoke license (admin only)
        try {
            await testRevokeLicense();
        } catch (error) {
            if (error.response?.status === 403) {
                console.log('⚠️ Cannot revoke license (admin rights required). Skipping revocation test.');
            } else {
                throw error;
            }
        }
        
        console.log('\n🎉 All tests completed successfully!');
        
    } catch (error) {
        console.error('\n💥 Test suite failed:', error.message);
        process.exit(1);
    }
}

// Run tests if this file is executed directly
if (require.main === module) {
    runAllTests();
}

module.exports = {
    runAllTests,
    registerUser,
    loginUser,
    testGetLicenseMe,
    testCreateLicense,
    testFeatureCheck,
    testModuleCheck,
    testLimitCheck,
    testRevokeLicense
};
