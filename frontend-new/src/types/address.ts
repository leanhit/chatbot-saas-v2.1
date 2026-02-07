// types/address.ts

/**
 * Khớp hoàn toàn với OwnerType.java backend
 * Chỉ có USER và TENANT, không có END_USER/ORDER/SHOP
 */
export type OwnerType = 'USER' | 'TENANT';

/**
 * Khớp với AddressEmbedded.java - Dùng cho Tenant/User
 * Backend chỉ dùng model này, không dùng Address table
 */
export interface AddressEmbedded {
  houseNumber?: string;
  street?: string;
  ward?: string;
  district?: string;
  province?: string;
  country?: string;
  fullAddress?: string;
}

// ========================
// HELPER TYPES
// ========================

/**
 * Type cho address của Tenant/User (dùng AddressEmbedded)
 */
export type TenantAddress = AddressEmbedded;
export type UserAddress = AddressEmbedded;

// ========================
// LEGACY TYPES (Để giữ compatibility nhưng sẽ deprecated)
// ========================

/**
 * @deprecated Dùng AddressEmbedded thay thế
 * Backend không sử dụng Address table cho User/Tenant
 */
export interface Address {
  id: number;
  tenantId: number;
  ownerType: OwnerType;
  ownerId: number;
  houseNumber?: string;
  street?: string;
  ward?: string;
  district?: string;
  province?: string;
  country?: string;
  isDefault: boolean; // Cần thiết cho nhiều addresses
}

/**
 * @deprecated Dùng AddressEmbedded thay thế
 */
export interface AddressResponseDTO {
  id: number;
  fullAddress: string;
  isDefault: boolean;
}

/**
 * @deprecated Dùng AddressEmbedded thay thế
 */
export interface AddressDetailResponseDTO extends AddressResponseDTO {
  tenantId: number;
  ownerType: OwnerType;
  ownerId: number;

  houseNumber: string | null;
  street: string;
  ward: string;
  district: string | null;
  province: string | null;
  country: string | null;
}

/**
 * @deprecated Dùng AddressEmbedded thay thế
 */
export interface AddressRequestDTO {
  ownerType: OwnerType;
  ownerId: number;
  
  street: string;
  houseNumber?: string;
  ward: string;
  district?: string;
  province?: string;
  country?: string;
  isDefault: boolean; // Cần thiết cho nhiều addresses
}

// ========================
// NOTE: Backend chỉ hỗ trợ USER và TENANT addresses
// Không có END_USER, ORDER, SHOP như comment cũ
// ========================
