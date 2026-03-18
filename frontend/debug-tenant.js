// Debug and Fix Tenant Key Issue
console.log('=== Tenant Key Debug ===');

// Check current localStorage state
console.log('1. Current localStorage state:');
const currentTenantKey = localStorage.getItem('active_tenant_id');
const currentTenantData = localStorage.getItem('tenant_data');

console.log('Active Tenant ID:', currentTenantKey);
console.log('Tenant Data:', currentTenantData ? 'exists' : 'missing');

if (currentTenantData) {
  try {
    const parsed = JSON.parse(currentTenantData);
    console.log('Parsed tenant data:', {
      id: parsed.id,
      tenantKey: parsed.tenantKey,
      name: parsed.name,
      status: parsed.status
    });
  } catch (e) {
    console.error('Error parsing tenant data:', e);
  }
}

// Force set the correct tenant key if it's missing or wrong
const correctTenantKey = '3a7df232-1818-4b43-9105-c0f33597f4b2';
const correctTenantData = {
  id: 1,
  tenantKey: correctTenantKey,
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

console.log('\n2. Setting correct tenant data...');
try {
  localStorage.setItem('tenant_data', JSON.stringify(correctTenantData));
  localStorage.setItem('active_tenant_id', correctTenantKey);
  
  // Verify it was set correctly
  const verifyKey = localStorage.getItem('active_tenant_id');
  const verifyData = JSON.parse(localStorage.getItem('tenant_data'));
  
  console.log('✅ Tenant key set successfully:', verifyKey);
  console.log('✅ Tenant data set successfully:', verifyData.name);
  console.log('✅ Keys match:', verifyKey === verifyData.tenantKey);
  
} catch (error) {
  console.error('❌ Error setting tenant data:', error);
}

console.log('\n3. Final verification:');
console.log('Final active_tenant_id:', localStorage.getItem('active_tenant_id'));
console.log('Final tenant_data exists:', !!localStorage.getItem('tenant_data'));

console.log('\n=== Debug Complete ===');
console.log('Now try accessing the Customer Data page again.');
