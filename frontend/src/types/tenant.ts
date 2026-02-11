/**
 * Tenant Types - Đồng bộ với Backend DTO
 * Based on: com.chatbot.modules.tenant.core.model.TenantStatus
 */

export enum TenantStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE', 
  SUSPENDED = 'SUSPENDED'
}

export enum TenantVisibility {
  PUBLIC = 'PUBLIC',
  PRIVATE = 'PRIVATE'
}

export enum MembershipStatus {
  NONE = 'NONE',
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED'
}

export enum JoinRequestStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED', 
  REJECTED = 'REJECTED',
  REVIEWING = 'REVIEWING'
}

export enum InvitationStatus {
  PENDING = 'PENDING',
  ACCEPTED = 'ACCEPTED',
  REJECTED = 'REJECTED',
  EXPIRED = 'EXPIRED'
}

export interface TenantBasicInfo {
  tenantKey: string
  name: string
  status: TenantStatus
  visibility: TenantVisibility
  expiresAt?: string
  createdAt: string
  logoUrl?: string
  contactEmail?: string
  contactPhone?: string
}

export interface TenantProfile {
  tenantKey: string
  name: string
  description?: string
  logoUrl?: string
  contactEmail?: string
  contactPhone?: string
  website?: string
  address?: Address
}

export interface Address {
  id: string
  street: string
  ward?: string
  district: string
  province: string
  postalCode?: string
  country?: string
  ownerType: 'USER' | 'TENANT'
  ownerId: string
}

export interface JoinRequest {
  id: string
  userId: string
  tenantId: string
  requestedRole: 'MEMBER' | 'ADMIN' | 'EDITOR'
  status: JoinRequestStatus
  message?: string
  requestedAt: string
  user?: {
    id: string
    email: string
    name: string
    avatar?: string
  }
}

export interface Invitation {
  id: string
  tenantId: string
  invitedEmail: string
  invitedBy: string
  invitedByName: string
  role: 'MEMBER' | 'ADMIN' | 'EDITOR'
  status: InvitationStatus
  message?: string
  expiresAt: string
  invitedAt: string
}

export interface TenantMember {
  id: string
  userId: string
  tenantId: string
  role: 'MEMBER' | 'ADMIN' | 'EDITOR'
  status: 'ACTIVE' | 'INACTIVE'
  joinedAt: string
  user?: {
    id: string
    email: string
    name: string
    avatar?: string
  }
}

// Status mappings cho UI
export const TENANT_STATUS_LABELS = {
  [TenantStatus.ACTIVE]: 'Active',
  [TenantStatus.INACTIVE]: 'Inactive',
  [TenantStatus.SUSPENDED]: 'Suspended'
} as const

export const TENANT_VISIBILITY_LABELS = {
  [TenantVisibility.PUBLIC]: 'Public',
  [TenantVisibility.PRIVATE]: 'Private'
} as const

export const MEMBERSHIP_STATUS_LABELS = {
  [MembershipStatus.NONE]: 'None',
  [MembershipStatus.PENDING]: 'Pending',
  [MembershipStatus.APPROVED]: 'Member',
  [MembershipStatus.REJECTED]: 'Rejected'
} as const

// Status badge classes cho Tailwind
export const TENANT_STATUS_CLASSES = {
  [TenantStatus.ACTIVE]: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
  [TenantStatus.INACTIVE]: 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200',
  [TenantStatus.SUSPENDED]: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
} as const

export const MEMBERSHIP_STATUS_CLASSES = {
  [MembershipStatus.NONE]: 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200',
  [MembershipStatus.PENDING]: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
  [MembershipStatus.APPROVED]: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
  [MembershipStatus.REJECTED]: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
} as const
