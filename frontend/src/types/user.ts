import type { SystemRole } from './auth';
import type { AddressDetailResponseDTO } from './address';

/**
 * Khớp với UserInfoResponse.java
 */
export interface UserInfoResponse {
  id: number;
  email: string; // Kết hợp từ Auth Entity ở Backend
  fullName: string | null;
  phoneNumber: string | null;
  avatar: string | null;
  gender: string | null;
  bio: string | null;
}

/**
 * Khớp với UserInfoRequest.java
 */
export interface UserInfoRequest {
  fullName: string;
  phoneNumber: string;
  avatar: string;
  bio: string;
}

/**
 * Cấu trúc User đầy đủ bao gồm cả thông tin Auth và Profile
 */
export interface UserFullAccount {
  id: number;
  email: string;
  systemRole: SystemRole;
  isActive: boolean;
  profile: UserInfoResponse;
  addresses: AddressDetailResponseDTO[];
}
