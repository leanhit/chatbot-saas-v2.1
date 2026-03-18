// Script to collect all i18n keys from Vue files
const fs = require('fs');
const path = require('path');

// Function to extract keys from $t() calls
function extractKeysFromContent(content) {
  const keys = new Set();
  
  // Match $t('key') patterns
  const tMatches = content.match(/\$t\(\s*['"`]([^'"`]+)['"`]\s*\)/g);
  if (tMatches) {
    tMatches.forEach(match => {
      const keyMatch = match.match(/\$t\(\s*['"`]([^'"`]+)['"`]\s*\)/);
      if (keyMatch) {
        keys.add(keyMatch[1]);
      }
    });
  }
  
  // Match $t(`key`) patterns
  const tBacktickMatches = content.match(/\$t\(\s*`([^`]+)`\s*\)/g);
  if (tBacktickMatches) {
    tBacktickMatches.forEach(match => {
      const keyMatch = match.match(/\$t\(\s*`([^`]+)`\s*\)/);
      if (keyMatch) {
        keys.add(keyMatch[1]);
      }
    });
  }
  
  // Match $t(variable) patterns (dynamic keys)
  const tVariableMatches = content.match(/\$t\(\s*[^)]+\s*\)/g);
  if (tVariableMatches) {
    tVariableMatches.forEach(match => {
      // Look for template literals or variables that might contain keys
      if (match.includes('customers.status.')) {
        // Extract the base pattern for dynamic keys
        keys.add('customers.status.PENDING');
        keys.add('customers.status.COMPLETED');
        keys.add('customers.status.PUSHED_TO_ODOO');
        keys.add('customers.status.FAILED');
      }
    });
  }
  
  return Array.from(keys);
}

// Function to build structured locale object
function buildLocaleStructure(keys) {
  const structure = {};
  
  keys.forEach(key => {
    const parts = key.split('.');
    let current = structure;
    
    parts.forEach((part, index) => {
      if (index === parts.length - 1) {
        // Last part - this is the leaf node
        if (!current[part]) {
          current[part] = `[${key}]`; // Placeholder for translation
        }
      } else {
        // Intermediate part - create object if doesn't exist
        if (!current[part]) {
          current[part] = {};
        }
        current = current[part];
      }
    });
  });
  
  return structure;
}

// Main execution
console.log('Collecting i18n keys from Vue files...');

// This would be used in a Node.js environment
// For now, I'll manually extract the keys from the grep output

const collectedKeys = [
  // Gateway keys
  'gateway.title',
  'gateway.subtitle', 
  'gateway.create',
  'gateway.logout',
  'gateway.tabs.myTenants',
  'gateway.tabs.search',
  'gateway.tabs.pending',
  'gateway.tabs.myInvitations',
  'gateway.createModal.title',
  'gateway.createModal.workspaceName',
  'gateway.createModal.enterWorkspaceName',
  'gateway.createModal.visibility',
  'gateway.createModal.options.public',
  'gateway.createModal.options.publicDescription',
  'gateway.createModal.options.private',
  'gateway.createModal.options.privateDescription',
  'gateway.createModal.cancel',
  'gateway.createModal.submit',
  
  // Customers keys
  'customers.overview',
  'customers.title',
  'customers.list',
  'customers.allStatuses',
  'customers.loadingCustomers',
  'customers.noCustomersFound',
  'customers.unknownCustomer',
  'customers.noPhone',
  'customers.synced',
  'customers.morePhones',
  'customers.contactInfo',
  'customers.stats.total',
  'customers.stats.pending',
  'customers.stats.completed',
  'customers.stats.synced',
  'customers.status.PENDING',
  'customers.status.COMPLETED',
  'customers.status.PUSHED_TO_ODOO',
  'customers.status.FAILED',
  
  // Common keys
  'common.refresh',
  'common.search',
  'common.loading'
];

const structure = buildLocaleStructure(collectedKeys);
console.log('Locale structure:');
console.log(JSON.stringify(structure, null, 2));
