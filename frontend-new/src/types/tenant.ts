import type { AddressEmbedded } from './address';

/** ========================
 * ENUMS (Khớp 100% Java Enum)
 * ======================== */
export type TenantStatus = 'ACTIVE' | 'SUSPENDED' | 'INACTIVE';
export type TenantVisibility = 'PUBLIC' | 'PRIVATE';
export type TenantRole = 'OWNER' | 'ADMIN' | 'EDITOR' | 'VIEWER' | 'MEMBER' | 'NONE';

/**
 * Trạng thái của thành viên trong bảng TenantMember
 */
export type MembershipStatus = 'PENDING' | 'INVITED' | 'ACTIVE' | 'REJECTED' | 'BLOCKED';

/**
 * Trạng thái yêu cầu tham gia
 */
export type TenantMembershipStatus = 'NONE' | 'PENDING' | 'APPROVED' | 'REJECTED';

/**
 * Trạng thái lời mời từ InvitationStatus.java
 */
export type InvitationStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'EXPIRED' | 'REVOKED';

/** ========================
 * CORE TENANT MODELS
 * ======================== */

/**
 * Khớp với Tenant.java backend
 */
export interface Tenant {
  id: number; // Internal ID (backend only)
  tenantKey: string; // ✅ UUID for frontend
  name: string;
  status: TenantStatus;
  visibility: TenantVisibility;
  expiresAt?: string;
  createdAt: string;
  updatedAt: string;
  profile?: TenantProfile;
}

/**
 * Khớp với TenantProfile.java backend
 */
export interface TenantProfile {
  id: number; // Sẽ dùng chung ID với Tenant
  tenant: Tenant;
  description?: string;
  legalName?: string;
  taxCode?: string;
  contactEmail?: string;
  contactPhone?: string;
  logoUrl?: string;
  faviconUrl?: string;
  primaryColor?: string;
  createdAt: string;
  updatedAt: string;
}

export interface TenantProfileResponse {
  tenantId: number;
  legalName?: string;
  taxCode?: string;
  contactEmail?: string;
  contactPhone?: string;
  logoUrl?: string;
  faviconUrl?: string;
  primaryColor?: string;
  description?: string;
  website?: string;
  industry?: string;
  size?: string;
}

export interface TenantDetailResponse {
  id: number; // Internal ID (backend only)
  tenantKey: string; // ✅ UUID for frontend
  name: string;
  status: TenantStatus;
  visibility: TenantVisibility;
  expiresAt: string | null;
  createdAt: string;
  profile: TenantProfileResponse;
  address: AddressEmbedded | null; // Dùng AddressEmbedded cho tenant
}

export interface TenantResponse {
  id: number; // Internal ID (backend only)
  tenantKey: string; // ✅ UUID for frontend
  name: string;
  status: TenantStatus;
  visibility: TenantVisibility;
  expiresAt: string | null;
  createdAt: string;
}

export interface TenantSearchResponse {
  id: string; // UUID string from API
  name: string;
  status: TenantStatus;
  defaultLocale?: string | null;
  createdAt: string;
}

/** ========================
 * INVITATION MODELS (Khớp DTO Server)
 * ======================== */

// Khớp với InviteMemberRequest.java
export interface InviteMemberRequest {
  email: string;
  role: TenantRole;
  expiryDays: number;
}

// Khớp với InvitationResponse.java
export interface InvitationResponse {
  id: string;
  name: string;
  email: string;
  role: TenantRole;
  status: InvitationStatus;
  expiresAt: string; // ISO Date String
  invitedByName: string; // Tên của Auth người mời
}

/** ========================
 * MEMBERSHIP & JOIN REQUESTS
 * ======================== */

// Khớp với MemberResponse.java (Dùng cho bảng TenantMember)
export interface MemberResponse {
  id: number;
  userId: string;
  email: string;
  role: TenantRole;
  status: MembershipStatus;
  joinedAt: string | null;
  requestedAt: string | null;
}

// Khớp với TenantPendingResponse.java
export interface TenantPendingResponse {
  id: string;
  name: string;
  status: TenantMembershipStatus;
  visibility: TenantVisibility;
  requestedAt: string;
}

/** ========================
 * REQUEST PAYLOADS (DTO)
 * ======================== */

export interface CreateTenantRequest {
  name: string;
  visibility: TenantVisibility;
}

export interface TenantBasicInfoRequest {
  name: string;
  status: TenantStatus;
  expiresAt: string | null;
  visibility: TenantVisibility;
}

export interface TenantProfileRequest {
  legalName?: string;
  taxCode?: string;
  contactEmail?: string;
  contactPhone?: string;
  logoUrl?: string;
  faviconUrl?: string;
  primaryColor?: string;
  description?: string;
  website?: string;
  industry?: string;
  size?: string;
}

export interface UpdateJoinRequest {
  status: 'ACTIVE' | 'REJECTED';
}

export interface UpdateMemberRoleRequest {
  role: TenantRole;
}