export interface UserSession {
  userId: number;
  username: string;
  fullName: string;
  role: 'ADMIN' | 'SUPERVISOR' | 'OPERADOR' | 'CONSULTA';
}

export interface StoredToken {
  accessToken: string;
  expiresAt: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresAt: string;
  user: UserSession;
}
