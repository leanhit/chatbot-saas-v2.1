// Immediate Fix for Tenant Key Issue
console.log('=== Immediate Tenant Key Fix ===');

// Check current state
const currentKey = localStorage.getItem('active_tenant_id');
console.log('Current tenant key:', currentKey);

// Force set correct tenant key
const correctKey = '3a7df232-1818-4b43-9105-c0f33597f4b2';
const correctData = {
  id: 1,
  tenantKey: correctKey,
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

// Set the correct data
localStorage.setItem('active_tenant_id', correctKey);
localStorage.setItem('tenant_data', JSON.stringify(correctData));

// Verify
const verifyKey = localStorage.getItem('active_tenant_id');
console.log('✅ Fixed tenant key to:', verifyKey);

// Clear any potential browser cache issues
console.log('✅ Tenant key fixed. Please refresh the Customer Data page.');
