#!/bin/bash

# Script to check for @Transactional methods without explicit transactionManager specification
# This helps prevent accidental use of the default sharedTransactionManager in multi-datasource environment

echo "Checking for @Transactional methods without transactionManager specification..."
echo "============================================================================="

# Find all @Transactional annotations in service classes
# Exclude @TransactionalWith (custom annotation) and those with transactionManager/value specified
cd "$(dirname "$0")/.."

# Check method-level @Transactional without transactionManager
echo ""
echo "Method-level @Transactional without transactionManager:"
echo "-------------------------------------------------------"
grep -r "@Transactional" src/main/java/com/chatbot/core/*/service/*.java | \
    grep -v "@TransactionalWith" | \
    grep -v "transactionManager" | \
    grep -v "value =" | \
    grep -v "//.*@Transactional" | \
    grep -v "readOnly = true" || echo "None found"

# Check class-level @Transactional without transactionManager
echo ""
echo "Class-level @Transactional without transactionManager:"
echo "-------------------------------------------------------"
grep -B5 "^@Service\|^@Component" src/main/java/com/chatbot/core/*/service/*.java | \
    grep -A1 "@Transactional" | \
    grep -v "@TransactionalWith" | \
    grep -v "transactionManager" | \
    grep -v "value =" | \
    grep -v "//.*@Transactional" | \
    grep "@Transactional" || echo "None found"

echo ""
echo "============================================================================="
echo "Check complete. If any violations found above, please add transactionManager."
echo ""
echo "Example correct usage:"
echo "  @Transactional(value = \"userTransactionManager\", rollbackFor = Exception.class)"
echo "  @Transactional(transactionManager = \"tenantTransactionManager\", readOnly = true)"
echo ""
echo "Or use the type-safe custom annotation:"
echo "  @TransactionalWith(UserTransactionManager.class)"
echo "  @Transactional(value = \"userTransactionManager\", rollbackFor = Exception.class)"
