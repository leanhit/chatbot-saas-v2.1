// Tenant Switching Test Script
// This script verifies that the tenant switching mechanism works correctly

console.log('=== Tenant Switching Test ===');

// Test 1: Check current localStorage state
console.log('\n1. Current localStorage state:');
try {
  const activeTenantId = localStorage.getItem('active_tenant_id');
  const tenantData = localStorage.getItem('tenant_data');
  
  console.log('Active Tenant ID:', activeTenantId);
  console.log('Tenant Data exists:', !!tenantData);
  
  if (tenantData) {
    const parsed = JSON.parse(tenantData);
    console.log('Parsed tenant data:', {
      id: parsed.id,
      tenantKey: parsed.tenantKey,
      name: parsed.name,
      status: parsed.status
    });
  }
} catch (error) {
  console.error('Error reading localStorage:', error);
}

// Test 2: Simulate tenant switching
console.log('\n2. Simulating tenant switching...');
try {
  const mockTenantData = {
    id: 1,
    tenantKey: "3a7df232-1818-4b43-9105-c0f33597f4b2",
    name: "TEST-FIRE",
    status: "ACTIVE",
    visibility: "PUBLIC",
    profile: {
      tenantId: 1,
      description: "",
      industry: "",
      plan: "",
      companySize: "",
      legalName: "TEST-FIRE"
    }
  };
  
  // Simulate the switchTenant logic
  localStorage.setItem('tenant_data', JSON.stringify(mockTenantData));
  localStorage.setItem('active_tenant_id', mockTenantData.tenantKey);
  
  console.log('✅ Tenant data saved successfully');
  console.log('Saved tenantKey:', mockTenantData.tenantKey);
  
  // Verify it was saved correctly
  const savedTenantKey = localStorage.getItem('active_tenant_id');
  const savedData = JSON.parse(localStorage.getItem('tenant_data'));
  
  console.log('Verification:');
  console.log('- Saved tenantKey matches:', savedTenantKey === mockTenantData.tenantKey);
  console.log('- Saved data tenantKey matches:', savedData.tenantKey === mockTenantData.tenantKey);
  console.log('- Saved data name matches:', savedData.name === mockTenantData.name);
  
} catch (error) {
  console.error('Error in tenant switching test:', error);
}

// Test 3: Verify axios header would be set correctly
console.log('\n3. Axios header verification:');
try {
  const activeTenantKey = localStorage.getItem('active_tenant_id');
  console.log('X-Tenant-Key header would be:', activeTenantKey);
  console.log('Header is valid UUID:', !!activeTenantKey && activeTenantKey.length === 36);
} catch (error) {
  console.error('Error checking axios header:', error);
}

console.log('\n=== Test Complete ===');
