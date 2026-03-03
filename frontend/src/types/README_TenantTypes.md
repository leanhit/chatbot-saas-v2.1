# Tenant Types - Frontend Type Safety Guide

## Overview
This guide explains how to use the centralized TypeScript types for tenant-related functionality, ensuring type safety and synchronization with backend DTOs.

## 📁 File Structure

```
frontend/src/
├── types/
│   ├── tenant.ts              # Main type definitions
│   └── README_TenantTypes.md  # This guide
├── utils/
│   └── tenantValidators.ts   # Validation utilities
├── api/
│   └── tenantApi.ts          # Typed API client
└── views/tenant/member/components/
    ├── TypedMemberTab.vue    # Example typed component
    └── TypedInviteModal.vue  # Example typed modal
```

## 🎯 Core Types

### 1. Tenant Types

```typescript
import type { TenantResponse, TenantDetailResponse } from '@/types/tenant'

// Basic tenant info
const tenant: TenantResponse = {
  id: 1,
  tenantKey: 'abc-123',
  name: 'My Workspace',
  status: 'ACTIVE',
  createdAt: '2024-01-01T00:00:00Z'
}

// Detailed tenant info
const detailedTenant: TenantDetailResponse = {
  ...tenant,
  description: 'A great workspace',
  contactEmail: 'admin@example.com',
  industry: 'Technology'
}
```

### 2. Member Types

```typescript
import type { MemberResponse, TenantRole, MembershipStatus } from '@/types/tenant'

// Member with full typing
const member: MemberResponse = {
  id: 1,
  userId: 123,
  tenantId: 1,
  name: 'John Doe',
  email: 'john@example.com',
  role: 'ADMIN',
  status: 'ACTIVE',
  joinedAt: '2024-01-01T00:00:00Z',
  updatedAt: '2024-01-01T00:00:00Z'
}

// Type-safe role handling
const role: TenantRole = 'ADMIN' // ✅ Valid
const invalidRole: TenantRole = 'MANAGER' // ❌ TypeScript error

// Type-safe status handling
const status: MembershipStatus = 'ACTIVE' // ✅ Valid
```

### 3. API Types

```typescript
import type { InviteMemberRequest, UpdateMemberRoleRequest } from '@/types/tenant'

// Type-safe API requests
const inviteRequest: InviteMemberRequest = {
  email: 'user@example.com',
  role: 'MEMBER',
  message: 'Welcome to our team!',
  expiryDays: 7
}

const roleUpdate: UpdateMemberRoleRequest = {
  role: 'ADMIN'
}
```

## 🔧 Validation Utilities

### 1. Type Guards

```typescript
import { isValidTenantRole, isValidMembershipStatus } from '@/utils/tenantValidators'

// Validate roles
const role = 'ADMIN'
if (isValidTenantRole(role)) {
  // TypeScript knows role is TenantRole here
  console.log(`Valid role: ${role}`)
}

// Validate status
const status = 'ACTIVE'
if (isValidMembershipStatus(status)) {
  // TypeScript knows status is MembershipStatus here
  console.log(`Valid status: ${status}`)
}
```

### 2. Data Sanitization

```typescript
import { sanitizeMemberArray, sanitizeMemberData } from '@/utils/tenantValidators'

// Sanitize API response
const apiResponse = await api.getMembers()
const members = sanitizeMemberArray(apiResponse.data) // MemberResponse[]

// Sanitize single member
const member = sanitizeMemberData(rawMemberData) // MemberResponse | null
```

### 3. Permission Checks

```typescript
import { canModifyRole, getRoleHierarchy } from '@/utils/tenantValidators'

// Check if user can modify target role
if (canModifyRole('ADMIN', 'MEMBER')) {
  // Allow role change
}

// Get role hierarchy level
const adminLevel = getRoleHierarchy('ADMIN') // 4
const memberLevel = getRoleHierarchy('MEMBER') // 1
```

## 🏗️ Component Usage

### 1. Typed Component Example

```vue
<script setup lang="ts">
import type { MemberResponse, TenantRole } from '@/types/tenant'
import { sanitizeMemberArray, filterMembersByQuery } from '@/utils/tenantValidators'

// Reactive typed data
const members = ref<MemberResponse[]>([])
const selectedRole = ref<TenantRole>('MEMBER')

// Type-safe API call
const loadMembers = async () => {
  const response = await tenantApi.getTenantMembers(tenantKey)
  members.value = sanitizeMemberArray(response.data)
}

// Type-safe filtering
const filteredMembers = computed(() => {
  let filtered = members.value
  
  if (searchQuery.value) {
    filtered = filterMembersByQuery(filtered, searchQuery.value)
  }
  
  return filtered
})
</script>
```

### 2. Form Handling

```vue
<script setup lang="ts">
import type { InviteMemberRequest } from '@/types/tenant'
import { isValidTenantRole, isValidEmail } from '@/utils/tenantValidators'

// Typed form data
const formData = reactive<InviteMemberRequest>({
  email: '',
  role: 'MEMBER',
  message: '',
  expiryDays: 7
})

// Type-safe validation
const validateForm = (): boolean => {
  if (!isValidEmail(formData.email)) {
    errors.value.email = 'Invalid email address'
    return false
  }
  
  if (!isValidTenantRole(formData.role)) {
    errors.value.role = 'Invalid role selected'
    return false
  }
  
  return true
}
</script>
```

## 🔍 Search and Filter

### 1. Type-Safe Filtering

```typescript
import { 
  filterMembersByQuery, 
  filterMembersByRole, 
  filterMembersByStatus,
  sortMembers 
} from '@/utils/tenantValidators'

// Filter by search query
const searchResults = filterMembersByQuery(members, 'john')

// Filter by role
const adminMembers = filterMembersByRole(members, 'ADMIN')

// Filter by status
const activeMembers = filterMembersByStatus(members, 'ACTIVE')

// Sort members
const sortedMembers = sortMembers(members, 'name', 'asc')
```

### 2. Advanced Filtering

```typescript
// Chain multiple filters
const filteredMembers = computed(() => {
  let result = members.value
  
  // Apply search
  if (searchQuery.value) {
    result = filterMembersByQuery(result, searchQuery.value)
  }
  
  // Apply role filter
  if (roleFilter.value) {
    result = filterMembersByRole(result, roleFilter.value)
  }
  
  // Apply status filter
  if (statusFilter.value) {
    result = filterMembersByStatus(result, statusFilter.value)
  }
  
  // Apply sorting
  result = sortMembers(result, sortBy.value, sortDirection.value)
  
  return result
})
```

## 🎨 UI Helpers

### 1. Display Utilities

```typescript
import { 
  getRoleLabel, 
  getStatusLabel, 
  getStatusBadgeClass,
  getRoleBadgeClass 
} from '@/utils/tenantValidators'

// Get display labels
const roleLabel = getRoleLabel('ADMIN') // 'Admin'
const statusLabel = getStatusLabel('ACTIVE') // 'Active'

// Get CSS classes
const badgeClass = getStatusBadgeClass('ACTIVE') // CSS class string
const roleBadgeClass = getRoleBadgeClass('ADMIN') // CSS class string
```

### 2. Template Usage

```vue
<template>
  <!-- Role badge with type safety -->
  <span :class="getRoleBadgeClass(member.role)">
    {{ getRoleLabel(member.role) }}
  </span>
  
  <!-- Status badge -->
  <span :class="getStatusBadgeClass(member.status)">
    {{ getStatusLabel(member.status) }}
  </span>
</template>
```

## 🛡️ Error Handling

### 1. API Error Handling

```typescript
import type { ApiResponseError } from '@/types/tenant'

try {
  const response = await tenantApi.getTenantMembers(tenantKey)
  // Handle success
} catch (error: any) {
  const apiError = error as ApiResponseError
  
  switch (apiError.code) {
    case 'RUNTIME_ERROR':
      console.error('Backend error:', apiError.message)
      break
    case 'VALIDATION_ERROR':
      console.error('Validation failed:', apiError.errors)
      break
    default:
      console.error('Unknown error:', apiError.message)
  }
}
```

### 2. Data Validation

```typescript
import { validateMemberResponse } from '@/utils/tenantValidators'

// Validate API response
const apiData = await api.getMember(memberId)
if (validateMemberResponse(apiData)) {
  // TypeScript knows apiData is MemberResponse
  console.log('Member name:', apiData.name)
} else {
  console.error('Invalid member data received')
}
```

## 📋 Best Practices

### 1. Always Use Types

```typescript
// ✅ Good - Typed
const members: MemberResponse[] = []

// ❌ Bad - Any
const members: any[] = []
```

### 2. Validate External Data

```typescript
// ✅ Good - Validate API data
const members = sanitizeMemberArray(apiResponse.data)

// ❌ Bad - Trust API data
const members = apiResponse.data
```

### 3. Use Type Guards

```typescript
// ✅ Good - Type guards
if (isValidTenantRole(role)) {
  // TypeScript knows role is TenantRole
}

// ❌ Bad - Assume type
const role = userInput as TenantRole // Unsafe
```

### 4. Handle Null/Undefined

```typescript
// ✅ Good - Defensive programming
const memberName = member?.name || 'Unknown'

// ❌ Bad - Assume data exists
const memberName = member.name // Could be undefined
```

## 🔄 Migration Guide

### From JavaScript to TypeScript

1. **Add type imports:**
```typescript
import type { MemberResponse, TenantRole } from '@/types/tenant'
```

2. **Type reactive data:**
```typescript
// Before
const members = ref([])

// After
const members = ref<MemberResponse[]>([])
```

3. **Add type guards:**
```typescript
// Before
if (['OWNER', 'ADMIN'].includes(role)) {

// After
if (isValidTenantRole(role)) {
```

4. **Use typed API:**
```typescript
// Before
const response = await api.getMembers()

// After
const response: ApiResponse<MemberResponse[]> = await tenantApi.getTenantMembers(tenantKey)
```

## 🚀 Advanced Usage

### 1. Custom Type Guards

```typescript
// Create custom type guard for active admins
function isActiveAdmin(member: any): member is MemberResponse & { role: 'ADMIN' } {
  return validateMemberResponse(member) && member.role === 'ADMIN' && member.status === 'ACTIVE'
}

// Usage
if (isActiveAdmin(member)) {
  // TypeScript knows member is an active admin
  console.log(`${member.name} is an active admin`)
}
```

### 2. Generic Components

```typescript
// Generic list component
interface TypedListProps<T> {
  items: T[]
  renderItem: (item: T) => VNode
  filterFn?: (item: T) => boolean
}

// Usage with member type
const MemberList: TypedListProps<MemberResponse> = {
  items: members,
  renderItem: (member) => h('div', member.name),
  filterFn: (member) => member.status === 'ACTIVE'
}
```

### 3. Type-Safe Events

```typescript
// Define typed events
interface MemberEvents {
  'member-updated': (member: MemberResponse) => void
  'member-removed': (memberId: number) => void
}

// Use in component
const emit = defineEmits<MemberEvents>()

// Type-safe event emission
emit('member-updated', updatedMember) // ✅ Type checked
emit('member-removed', member.id)     // ✅ Type checked
```

## 🎯 Benefits

1. **Type Safety** - Catch errors at compile time
2. **Auto-completion** - Better IDE support
3. **Documentation** - Types serve as documentation
4. **Refactoring** - Safe code refactoring
5. **Team Consistency** - Shared type definitions
6. **Backend Sync** - Aligned with backend DTOs

## 🔗 Related Files

- [`/types/tenant.ts`](./tenant.ts) - Main type definitions
- [`/utils/tenantValidators.ts`](../utils/tenantValidators.ts) - Validation utilities
- [`/api/tenantApi.ts`](../api/tenantApi.ts) - Typed API client
- [`/views/tenant/member/components/TypedMemberTab.vue`](../views/tenant/member/components/TypedMemberTab.vue) - Example component
- [`/views/tenant/member/components/TypedInviteModal.vue`](../views/tenant/member/components/TypedInviteModal.vue) - Example modal
