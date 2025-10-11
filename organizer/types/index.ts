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

export interface PaginatedResponse<T> {
    content: T[];
    pageable: {
        pageNumber: number;
        pageSize: number;
    };
    last: boolean;
    totalPages: number;
    totalElements: number;
    first: boolean;
    numberOfElements: number;
    size: number;
    number: number;
    empty: boolean;
}

export interface AttendeeRequest {
    uniqueIdentifier: string;
    firstName: string;
    lastName: string;
    customFields?: Record<string, unknown>;
}

export interface AttendeeResponse {
    id: number;
    uniqueIdentifier: string;
    firstName: string;
    lastName: string;
    customFields?: Record<string, unknown>;
}

export interface ApiErrorResponse {
    timestamp: string;
    status: number;
    error: string;
    message: string;
    path: string;
    validationErrors?: Record<string, string>;
}

export interface RoleResponse {
    id: number;
    name: string;
    permissions: string[];
}

export interface OrganizerResponse {
    id: number;
    email: string;
    roleName: string;
    roleId: number;
}

export interface UserCreateRequest {
    email: string;
    roleId: number;
    temporaryPassword: string;
}

export interface OrganizerRoleUpdateRequest {
    roleId: number;
}

export type FieldType = 'TEXT' | 'NUMBER' | 'DATE' | 'SELECT';

export interface CustomFieldDefinition {
    id: number;
    fieldName: string;
    fieldType: FieldType;
    options?: string[];
}

export interface CustomFieldDefinitionRequest {
    fieldName: string;
    fieldType: FieldType;
    options?: string[];
}
