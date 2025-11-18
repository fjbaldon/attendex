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

export type TimeSlotType = 'CHECK_IN' | 'CHECK_OUT';

export interface TimeSlot {
    id?: number;
    activityName: string;
    targetTime: Date;
    type: TimeSlotType;
}

export interface EventRequest {
    eventName: string;
    startDate: Date;
    endDate: Date;
    onTimeWindowMinutesBefore: number;
    onTimeWindowMinutesAfter: number;
    timeSlots: TimeSlot[];
}

export interface EventResponse {
    id: number;
    eventName: string;
    startDate: string;
    endDate: string;
    onTimeWindowMinutesBefore: number;
    onTimeWindowMinutesAfter: number;
    timeSlots: TimeSlot[];
    status: 'ACTIVE' | 'ONGOING' | 'UPCOMING' | 'PAST';
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

export interface OrganizerResponse {
    id: number;
    email: string;
}

export interface ScannerResponse {
    id: number;
    email: string;
}

export interface UserCreateRequest {
    email: string;
    temporaryPassword: string;
}

export type FieldType = 'SELECT';

export interface CustomFieldDefinition {
    id: number;
    fieldName: string;
    fieldType: FieldType;
    options?: string[];
}

export interface CustomFieldDefinitionRequest {
    fieldName: string;
    options: string[];
}

export interface AnalyticsBreakdownItem {
    groupName: string;
    count: number;
}

export interface AnalyticsBreakdownDto {
    breakdown: AnalyticsBreakdownItem[];
    totalCheckedIn: number;
}

export interface DashboardStats {
    totalEvents: number;
    totalAttendees: number;
    totalScanners: number;
    liveCheckIns: number;
}

export interface DailyActivity {
    date: string;
    count: number;
}

export interface Organization {
    id: number;
    name: string;
    identifierFormatRegex: string | null;
    status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
    subscriptionType: 'LIFETIME' | 'ANNUAL' | 'TRIAL';
    subscriptionExpiresAt: string | null;
}

export interface CheckedInAttendeeResponse extends AttendeeResponse {
    checkInTimestamp: string;
}

export interface UpcomingEvent {
    id: number;
    eventName: string;
    startDate: string;
}

export interface RecentEventStats {
    id: number;
    eventName: string;
    totalRegistered: number;
    totalCheckedIn: number;
}

export interface DashboardData {
    stats: DashboardStats;
    upcomingEvents: UpcomingEvent[];
    recentEventStats: RecentEventStats[];
}

export interface InvalidRow {
    rowNumber: number;
    rowData: Record<string, string>;
    error: string;
}

export interface AttendeeImportAnalysis {
    validAttendees: AttendeeRequest[];
    invalidRows: InvalidRow[];
}

export interface AdminDashboardStats {
    totalOrganizations: number;
    activeOrganizations: number;
    trialSubscriptions: number;
    suspendedAccounts: number;
}

export interface OrganizationSummary {
    id: number;
    name: string;
    date: string;
}

export interface DailyRegistration {
    date: string;
    count: number;
}

export interface AdminDashboardData {
    stats: AdminDashboardStats;
    expiringSubscriptions: OrganizationSummary[];
    recentRegistrations: OrganizationSummary[];
    attentionRequired: OrganizationSummary[];
}

export interface SystemAdmin {
    id: number;
    email: string;
    createdAt: string;
}

export interface SystemAdminCreateRequest {
    email: string;
    temporaryPassword: string;
}
