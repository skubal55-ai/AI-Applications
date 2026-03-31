export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  role: 'CUSTOMER' | 'STAFF' | 'ADMIN';
  createdAt: string;
}

export interface AuthRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone: string;
}
