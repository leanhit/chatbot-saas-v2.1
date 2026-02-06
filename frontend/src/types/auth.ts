/**
 * Khớp với SystemRole.java
 */
export type SystemRole = 'USER' | 'ADMIN';

/**
 * Khớp với UserDto.java
 * Thông tin định danh cơ bản
 */
export interface UserDto {
  id: number;
  email: string;
  systemRole: SystemRole;
}

/**
 * Khớp với UserResponse.java
 * Kết quả trả về sau khi Login/Register
 */
export interface UserResponse {
  token: string;
  user: UserDto;
}

/**
 * Khớp với LoginRequest.java & RegisterRequest.java
 */
export interface LoginRequest {
  email: string;
  password: string;
}

export type RegisterRequest = LoginRequest;

/**
 * Khớp với ChangePasswordRequest.java
 */
export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}

/**
 * Khớp với ChangeRoleRequest.java
 */
export interface ChangeRoleRequest {
  userId: number;
  newRole: SystemRole;
}