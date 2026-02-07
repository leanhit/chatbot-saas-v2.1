// types/user.ts

/**
 * Khớp hoàn toàn với UserDto.java backend
 */
export interface UserDto {
  id: string;        // UUID in Java, represented as string in TypeScript
  email: string;
  systemRole: string;
  locale: string;
}
