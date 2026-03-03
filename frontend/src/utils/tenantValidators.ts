/**
 * Tenant Type Validators and Utilities
 * Provides type guards and validation functions for tenant-related data
 */

import type { 
  TenantResponse, 
  MemberResponse, 
  InvitationResponse, 
  JoinRequestResponse,
  TenantRole,
  MembershipStatus
} from '@/types/tenant'

// ============================================================================
// TENANT ROLE VALIDATORS
// ============================================================================

/**
 * Valid tenant roles (matches backend enum)
 */
export const VALID_TENANT_ROLES = ['OWNER', 'ADMIN', 'EDITOR', 'VIEWER', 'MEMBER', 'NONE'] as const

/**
 * Check if a value is a valid TenantRole
 */
export function isValidTenantRole(value: string): value is TenantRole {
  return VALID_TENANT_ROLES.includes(value as TenantRole)
}

/**
 * Get role hierarchy level (higher number = higher privilege)
 */
export function getRoleHierarchy(role: TenantRole): number {
  const hierarchy: Record<TenantRole, number> = {
    'OWNER': 5,
    'ADMIN': 4,
    'EDITOR': 3,
    'VIEWER': 2,
    'MEMBER': 1,
    'NONE': 0
  }
  return hierarchy[role] || 0
}

/**
 * Check if role1 can modify role2
 */
export function canModifyRole(userRole: TenantRole, targetRole: TenantRole): boolean {
  const userLevel = getRoleHierarchy(userRole)
  const targetLevel = getRoleHierarchy(targetRole)
  
  // Cannot modify OWNER unless you are OWNER
  if (targetRole === 'OWNER' && userRole !== 'OWNER') {
    return false
  }
  
  // Can only modify roles with lower or equal hierarchy
  return userLevel >= targetLevel
}

// ============================================================================
// MEMBERSHIP STATUS VALIDATORS
// ============================================================================

/**
 * Valid membership statuses (matches backend enum)
 */
export const VALID_MEMBERSHIP_STATUSES = ['PENDING', 'ACTIVE', 'INACTIVE', 'REJECTED', 'EXPIRED', 'REVOKED'] as const

/**
 * Check if a value is a valid MembershipStatus
 */
export function isValidMembershipStatus(value: string): value is MembershipStatus {
  return VALID_MEMBERSHIP_STATUSES.includes(value as MembershipStatus)
}

/**
 * Check if status allows member actions
 */
export function isActiveMember(status: MembershipStatus): boolean {
  return status === 'ACTIVE'
}

/**
 * Check if status is pending approval
 */
export function isPendingStatus(status: MembershipStatus): boolean {
  return status === 'PENDING'
}

// ============================================================================
// TENANT DATA VALIDATORS
// ============================================================================

/**
 * Validate tenant response data
 */
export function validateTenantResponse(data: any): data is TenantResponse {
  return (
    data &&
    typeof data.id === 'number' &&
    typeof data.tenantKey === 'string' &&
    typeof data.name === 'string' &&
    ['ACTIVE', 'INACTIVE', 'PENDING'].includes(data.status) &&
    typeof data.createdAt === 'string'
  )
}

/**
 * Validate member response data
 */
export function validateMemberResponse(data: any): data is MemberResponse {
  return (
    data &&
    typeof data.id === 'number' &&
    typeof data.userId === 'number' &&
    typeof data.tenantId === 'number' &&
    typeof data.name === 'string' &&
    typeof data.email === 'string' &&
    isValidTenantRole(data.role) &&
    isValidMembershipStatus(data.status) &&
    typeof data.joinedAt === 'string'
  )
}

/**
 * Validate invitation response data
 */
export function validateInvitationResponse(data: any): data is InvitationResponse {
  return (
    data &&
    typeof data.id === 'number' &&
    typeof data.tenantId === 'number' &&
    typeof data.email === 'string' &&
    isValidTenantRole(data.role) &&
    isValidMembershipStatus(data.status) &&
    typeof data.token === 'string' &&
    typeof data.invitedBy === 'string' &&
    typeof data.invitedAt === 'string' &&
    typeof data.expiresAt === 'string'
  )
}

/**
 * Validate join request response data
 */
export function validateJoinRequestResponse(data: any): data is JoinRequestResponse {
  return (
    validateMemberResponse(data) &&
    typeof data.requestedRole === 'string' &&
    isValidTenantRole(data.requestedRole) &&
    typeof data.requestedAt === 'string'
  )
}

// ============================================================================
// DATA TRANSFORMATION UTILITIES
// ============================================================================

/**
 * Sanitize and validate member data from API
 */
export function sanitizeMemberData(data: any): MemberResponse | null {
  if (!validateMemberResponse(data)) {
    console.warn('Invalid member data received:', data)
    return null
  }
  
  return {
    ...data,
    // Ensure required fields have defaults
    name: data.name || 'Unknown',
    email: data.email || 'no-email@example.com',
    avatar: data.avatar || undefined,
    phone: data.phone || undefined,
    department: data.department || undefined,
    position: data.position || undefined
  }
}

/**
 * Sanitize array of member data
 */
export function sanitizeMemberArray(data: any[]): MemberResponse[] {
  if (!Array.isArray(data)) {
    console.warn('Expected array of member data, received:', typeof data)
    return []
  }
  
  return data
    .map(sanitizeMemberData)
    .filter((member): member is MemberResponse => member !== null)
}

/**
 * Get role display label
 */
export function getRoleLabel(role: TenantRole): string {
  const labels: Record<TenantRole, string> = {
    'OWNER': 'Owner',
    'ADMIN': 'Admin',
    'EDITOR': 'Editor',
    'VIEWER': 'Viewer',
    'MEMBER': 'Member',
    'NONE': 'None'
  }
  return labels[role] || role
}

/**
 * Get status display label
 */
export function getStatusLabel(status: MembershipStatus): string {
  const labels: Record<MembershipStatus, string> = {
    'PENDING': 'Pending',
    'ACTIVE': 'Active',
    'INACTIVE': 'Inactive',
    'REJECTED': 'Rejected',
    'EXPIRED': 'Expired',
    'REVOKED': 'Revoked'
  }
  return labels[status] || status
}

/**
 * Get status badge CSS class
 */
export function getStatusBadgeClass(status: MembershipStatus): string {
  const classes: Record<MembershipStatus, string> = {
    'PENDING': 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
    'ACTIVE': 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
    'INACTIVE': 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200',
    'REJECTED': 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
    'EXPIRED': 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200',
    'REVOKED': 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
  }
  return classes[status] || classes['INACTIVE']
}

/**
 * Get role badge CSS class
 */
export function getRoleBadgeClass(role: TenantRole): string {
  const classes: Record<TenantRole, string> = {
    'OWNER': 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
    'ADMIN': 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200',
    'EDITOR': 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
    'VIEWER': 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200',
    'MEMBER': 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
    'NONE': 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
  }
  return classes[role] || classes['NONE']
}

// ============================================================================
// SEARCH AND FILTER UTILITIES
// ============================================================================

/**
 * Filter members by search query
 */
export function filterMembersByQuery(members: MemberResponse[], query: string): MemberResponse[] {
  if (!query || query.trim() === '') {
    return members
  }
  
  const searchTerm = query.toLowerCase().trim()
  
  return members.filter(member => {
    const name = (member.name || '').toLowerCase()
    const email = (member.email || '').toLowerCase()
    const department = (member.department || '').toLowerCase()
    const position = (member.position || '').toLowerCase()
    
    return (
      name.includes(searchTerm) ||
      email.includes(searchTerm) ||
      department.includes(searchTerm) ||
      position.includes(searchTerm)
    )
  })
}

/**
 * Filter members by role
 */
export function filterMembersByRole(members: MemberResponse[], role: TenantRole | ''): MemberResponse[] {
  if (!role) return members
  return members.filter(member => member.role === role)
}

/**
 * Filter members by status
 */
export function filterMembersByStatus(members: MemberResponse[], status: MembershipStatus | ''): MemberResponse[] {
  if (!status) return members
  return members.filter(member => member.status === status)
}

/**
 * Sort members by field
 */
export function sortMembers(
  members: MemberResponse[], 
  sortBy: 'name' | 'email' | 'role' | 'joinedAt',
  direction: 'asc' | 'desc' = 'asc'
): MemberResponse[] {
  return [...members].sort((a, b) => {
    let aValue: string | number
    let bValue: string | number
    
    switch (sortBy) {
      case 'name':
        aValue = (a.name || '').toLowerCase()
        bValue = (b.name || '').toLowerCase()
        break
      case 'email':
        aValue = (a.email || '').toLowerCase()
        bValue = (b.email || '').toLowerCase()
        break
      case 'role':
        aValue = getRoleHierarchy(a.role)
        bValue = getRoleHierarchy(b.role)
        break
      case 'joinedAt':
        aValue = new Date(a.joinedAt).getTime()
        bValue = new Date(b.joinedAt).getTime()
        break
      default:
        aValue = a.name || ''
        bValue = b.name || ''
    }
    
    if (aValue < bValue) return direction === 'asc' ? -1 : 1
    if (aValue > bValue) return direction === 'asc' ? 1 : -1
    return 0
  })
}

// ============================================================================
// ERROR HANDLING UTILITIES
// ============================================================================

/**
 * Create safe member object with defaults
 */
export function createSafeMember(overrides: Partial<MemberResponse> = {}): MemberResponse {
  return {
    id: 0,
    userId: 0,
    tenantId: 0,
    name: 'Unknown',
    email: 'no-email@example.com',
    role: 'MEMBER',
    status: 'PENDING',
    joinedAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    ...overrides
  }
}

/**
 * Handle API response with validation
 */
export function handleMemberApiResponse(data: any): MemberResponse[] {
  try {
    // Handle paginated response
    if (data?.content && Array.isArray(data.content)) {
      return sanitizeMemberArray(data.content)
    }
    
    // Handle direct array response
    if (Array.isArray(data)) {
      return sanitizeMemberArray(data)
    }
    
    // Handle single member response
    if (validateMemberResponse(data)) {
      return [sanitizeMemberData(data)!]
    }
    
    console.warn('Unexpected API response format:', data)
    return []
  } catch (error) {
    console.error('Error processing member API response:', error)
    return []
  }
}

// ============================================================================
// EXPORTS
// ============================================================================

export default {
  // Validators
  isValidTenantRole,
  isValidMembershipStatus,
  validateTenantResponse,
  validateMemberResponse,
  validateInvitationResponse,
  validateJoinRequestResponse,
  
  // Utilities
  getRoleHierarchy,
  canModifyRole,
  isActiveMember,
  isPendingStatus,
  
  // Data transformation
  sanitizeMemberData,
  sanitizeMemberArray,
  
  // Display helpers
  getRoleLabel,
  getStatusLabel,
  getStatusBadgeClass,
  getRoleBadgeClass,
  
  // Search and filter
  filterMembersByQuery,
  filterMembersByRole,
  filterMembersByStatus,
  sortMembers,
  
  // Error handling
  createSafeMember,
  handleMemberApiResponse
}
