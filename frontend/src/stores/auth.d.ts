// Type declarations for auth store
export interface User {
  id: string;
  email: string;
  name: string;
  [key: string]: any;
}

export interface AuthState {
  user: User | null;
  token: string | null;
  refreshToken: string | null;
  isLoading: boolean;
  error: string | null;
}

export interface AuthStore {
  // State
  user: User | null;
  token: string | null;
  refreshToken: string | null;
  isLoading: boolean;
  error: string | null;
  
  // Getters
  isAuthenticated: boolean;
  
  // Actions
  login(credentials: { email: string; password: string }): Promise<{ token: string; refreshToken: string; user: User }>;
  register(userData: { name: string; email: string; password: string; confirmPassword: string }): Promise<{ success: boolean }>;
  loadCurrentUser(): Promise<User>;
  refreshTokens(): Promise<{ token: string; refreshToken: string }>;
  logout(): void;
  clearTokens(): void;
}

export declare const useAuthStore: () => AuthStore;
