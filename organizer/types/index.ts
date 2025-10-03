export interface AuthRequest {
    email?: string;
    password?: string;
}

export interface RegisterRequest {
    organizationName?: string;
    email?: string;
    password?: string;
}

export interface AuthResponse {
    accessToken: string;
    tokenType: string;
}

export interface DecodedToken {
    sub: string;
    organizationId: number;
    forcePasswordChange: boolean;
    roles: string[];
    iat: number;
    exp: number;
}

export interface EventRequest {
    eventName: string;
    startDate: Date;
    endDate: Date;
}

export interface EventResponse {
    id: number;
    eventName: string;
    startDate: string;
    endDate: string;
}

export interface ApiErrorResponse {
    timestamp: string;
    status: number;
    error: string;
    message: string;
    path: string;
    validationErrors?: Record<string, string>;
}
