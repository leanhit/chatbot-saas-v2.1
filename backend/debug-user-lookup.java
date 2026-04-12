// Debug script to check user lookup issue
// Run this in the backend context to debug the user lookup problem

// Test 1: Check database directly
SELECT id, email FROM users WHERE email = 'testuser@example.com';

// Test 2: Check if there's any caching issue
// The issue might be that the repository is returning a cached result

// Test 3: Check the CustomUserDetails construction
// The issue might be in how the CustomUserDetails is created

// Test 4: Check if there's any transaction isolation issue
// The issue might be that the user lookup is running in a different transaction context
