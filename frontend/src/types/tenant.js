// Frontend types matching backend DTOs

/**
 * Enums matching backend models
 */
export const TenantMembershipStatus = {
  NONE: 'NONE',        // Chưa yêu cầu, chưa tham gia
  PENDING: 'PENDING',    // Đang chờ duyệt  
  APPROVED: 'APPROVED',  // Đã là thành viên
  REJECTED: 'REJECTED'    // Bị từ chối
}

export const TenantStatus = {
  ACTIVE: 'ACTIVE',
  SUSPENDED: 'SUSPENDED',
  INACTIVE: 'INACTIVE'
}

export const TenantVisibility = {
  PUBLIC: 'PUBLIC',
  PRIVATE: 'PRIVATE'
}

export const TenantRole = {
  MEMBER: 'MEMBER',
  ADMIN: 'ADMIN',
  OWNER: 'OWNER'
}

export const InvitationStatus = {
  PENDING: 'PENDING',
  ACCEPTED: 'ACCEPTED',
  REJECTED: 'REJECTED',
  EXPIRED: 'EXPIRED'
}

export const MembershipStatus = {
  ACTIVE: 'ACTIVE',
  PENDING: 'PENDING',
  REJECTED: 'REJECTED',
  SUSPENDED: 'SUSPENDED'
}

/**
 * Request DTOs matching backend
 */
export const CreateTenantRequest = {
  name: '',
  visibility: TenantVisibility.PUBLIC
}

export const TenantBasicInfoRequest = {
  name: '',
  status: null,
  visibility: null,
  expiresAt: null // ISO 8601 string
}

export const TenantSearchRequest = {
  keyword: '',
  page: 0,
  size: 10,
  sortBy: 'name',
  sortDirection: 'asc'
}

export const TenantProfileRequest = {
  // business information
  description: '',
  industry: '',
  plan: '',
  companySize: '',
  
  // legal
  legalName: '',
  taxCode: '',
  
  // contact
  contactEmail: '',
  contactPhone: '',
  
  // branding
  logoUrl: '',
  faviconUrl: '',
  primaryColor: ''
}

export const InviteMemberRequest = {
  email: '',
  role: TenantRole.MEMBER,
  expiryDays: 7
}

/**
 * Response DTOs matching backend
 */
export const TenantResponse = {
  id: null,
  tenantKey: '',
  name: '',
  status: null,
  visibility: null,
  expiresAt: null, // ISO 8601 string
  createdAt: null, // ISO 8601 string
  logoUrl: '',
  contactEmail: '',
  contactPhone: ''
}

export const TenantDetailResponse = {
  id: null,
  tenantKey: '',
  name: '',
  status: null,
  visibility: null,
  expiresAt: null, // ISO 8601 string
  createdAt: null, // ISO 8601 string
  profile: null,
  address: null
}

export const TenantProfileResponse = {
  tenantId: null,
  
  // business information
  description: '',
  industry: '',
  plan: '',
  companySize: '',
  
  // legal
  legalName: '',
  taxCode: '',
  
  // contact
  contactEmail: '',
  contactPhone: '',
  
  // branding
  logoUrl: '',
  faviconUrl: '',
  primaryColor: ''
}

export const TenantSearchResponse = {
  id: null,
  tenantKey: '',
  name: '',
  status: null,
  visibility: null,
  createdAt: null, // ISO 8601 string
  membershipStatus: null,
  
  // Additional fields for identification
  logoUrl: '',
  contactEmail: '',
  province: ''
}

export const MemberResponse = {
  id: null,
  userId: null,
  email: '',
  role: null,
  status: null,
  joinedAt: null, // ISO 8601 string
  requestedAt: null // ISO 8601 string
}

export const InvitationResponse = {
  id: null,
  tenantId: null,
  inviteeEmail: '',
  inviterEmail: '',
  role: null,
  status: null,
  invitedAt: null, // ISO 8601 string
  expiresAt: null // ISO 8601 string
}

export const TenantPendingResponse = {
  id: null,
  tenantId: null,
  userEmail: '',
  status: null,
  requestedAt: null // ISO 8601 string
}

/**
 * Helper functions
 */
export const canJoinTenant = (membershipStatus) => {
  return membershipStatus === TenantMembershipStatus.NONE
}

export const getJoinButtonText = (membershipStatus, t) => {
  switch (membershipStatus) {
    case TenantMembershipStatus.NONE:
      return t('Join')
    case TenantMembershipStatus.PENDING:
      return t('Pending')
    case TenantMembershipStatus.APPROVED:
      return t('Member')
    case TenantMembershipStatus.REJECTED:
      return t('Rejected')
    default:
      return t('Join')
  }
}

export const getJoinButtonClass = (membershipStatus) => {
  switch (membershipStatus) {
    case TenantMembershipStatus.NONE:
      return 'join-button'
    case TenantMembershipStatus.PENDING:
      return 'pending-tag'
    case TenantMembershipStatus.APPROVED:
      return 'member-tag'
    case TenantMembershipStatus.REJECTED:
      return 'rejected-tag'
    default:
      return 'join-button'
  }
}

/**
 * Validation functions
 */
export const validateCreateTenantRequest = (request) => {
  const errors = []
  
  if (!request.name || request.name.trim().length === 0) {
    errors.push('Tenant name is required')
  }
  
  if (!request.visibility || !Object.values(TenantVisibility).includes(request.visibility)) {
    errors.push('Valid visibility is required')
  }
  
  return errors
}

export const validateInviteMemberRequest = (request) => {
  const errors = []
  
  if (!request.email || request.email.trim().length === 0) {
    errors.push('Email is required')
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(request.email)) {
    errors.push('Invalid email format')
  }
  
  if (!request.role || !Object.values(TenantRole).includes(request.role)) {
    errors.push('Valid role is required')
  }
  
  if (request.expiryDays && (request.expiryDays < 1 || request.expiryDays > 30)) {
    errors.push('Expiry days must be between 1 and 30')
  }
  
  return errors
}

export const validateTenantBasicInfoRequest = (request) => {
  const errors = []
  
  if (request.name && request.name.trim().length === 0) {
    errors.push('Tenant name cannot be empty')
  }
  
  if (request.status && !isValidTenantStatus(request.status)) {
    errors.push('Invalid tenant status')
  }
  
  if (request.visibility && !isValidTenantVisibility(request.visibility)) {
    errors.push('Invalid tenant visibility')
  }
  
  return errors
}

export const validateTenantProfileRequest = (request) => {
  const errors = []
  
  if (request.contactEmail && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(request.contactEmail)) {
    errors.push('Invalid contact email format')
  }
  
  if (request.contactPhone && request.contactPhone.trim().length === 0) {
    errors.push('Contact phone cannot be empty if provided')
  }
  
  return errors
}

export const validateTenantSearchRequest = (request) => {
  const errors = []
  
  if (request.page && (request.page < 0)) {
    errors.push('Page must be >= 0')
  }
  
  if (request.size && (request.size < 1 || request.size > 100)) {
    errors.push('Size must be between 1 and 100')
  }
  
  if (request.sortDirection && !['asc', 'desc'].includes(request.sortDirection)) {
    errors.push('Sort direction must be asc or desc')
  }
  
  return errors
}

/**
 * Type guards
 */
export const isValidTenantStatus = (status) => {
  return Object.values(TenantStatus).includes(status)
}

export const isValidTenantRole = (role) => {
  return Object.values(TenantRole).includes(role)
}

export const isValidTenantVisibility = (visibility) => {
  return Object.values(TenantVisibility).includes(visibility)
}

/**
 * Utility functions for date handling
 */
export const formatTenantDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString()
}

export const isTenantExpired = (expiresAt) => {
  if (!expiresAt) return false
  return new Date(expiresAt) < new Date()
}

/**
 * Default factory functions
 */
export const createDefaultTenantRequest = () => ({
  ...CreateTenantRequest
})

export const createDefaultTenantProfileRequest = () => ({
  ...TenantProfileRequest
})

export const createDefaultInviteMemberRequest = () => ({
  ...InviteMemberRequest
})

export const createDefaultTenantSearchRequest = () => ({
  ...TenantSearchRequest
})
