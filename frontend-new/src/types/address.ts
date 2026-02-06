// types/address.ts

/**
 * Khớp hoàn toàn với OwnerType.java
 */
export type OwnerType = 'USER' | 'END_USER' | 'ORDER' | 'SHOP';

/**
 * Khớp với Address.java backend
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
}

export interface AddressResponseDTO {
  id: number;
  fullAddress: string;
}

/**
 * Khớp hoàn toàn với AddressDetailResponseDTO.java
 */
export interface AddressDetailResponseDTO extends AddressResponseDTO {
  tenantId: number;
  ownerType: OwnerType;
  ownerId: number;

  houseNumber: string | null; // Java cho phép null
  street: string;             // Backend không còn @NotBlank
  ward: string;               // Backend không còn @NotBlank
  district: string | null;
  province: string | null;
  country: string | null;
}

/**
 * Khớp với AddressRequestDTO.java (Dùng cho cả Create và Update)
 */
export interface AddressRequestDTO {
  ownerType: OwnerType; // @NotNull
  ownerId: number;      // @NotNull
  
  street: string;       // Không còn @NotBlank
  houseNumber?: string;
  ward: string;         // Không còn @NotBlank
  district?: string;
  province?: string;
  country?: string;
}
