export interface AuthRequest {
    email?: string;
    password?: string;
}

export type RegisterRequest = AuthRequest;

export interface AuthResponse {
    accessToken: string;
    tokenType: string;
}
