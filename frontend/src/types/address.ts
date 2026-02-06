// types/address.ts

/**
 * Khớp hoàn toàn với OwnerType.java
 */
export type OwnerType = 'TENANT' | 'USER'; 

export interface AddressResponseDTO {
  id: number;
  fullAddress: string;
  isDefault: boolean;
}

/**
 * Khớp hoàn toàn với AddressDetailResponseDTO.java
 */
export interface AddressDetailResponseDTO extends AddressResponseDTO {
  tenantId: number;
  ownerType: OwnerType;
  ownerId: number;

  houseNumber: string | null; // Java cho phép null
  street: string;             // Backend @NotBlank nên không để dấu ?
  ward: string;               // Backend @NotBlank
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
  
  street: string;       // @NotBlank
  houseNumber?: string;
  ward: string;         // @NotBlank
  district?: string;
  province?: string;
  country?: string;
  isDefault: boolean;
}

// Nếu bạn muốn tách riêng Update để có ID (theo logic API PUT thông thường)
export interface UpdateAddressRequest extends AddressRequestDTO {
  id: number;
}