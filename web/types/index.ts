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
}

export interface DecodedToken {
    sub: string;
    organizationId: number;
    forcePasswordChange: boolean;
    roles: string[];
    iat: number;
    exp: number;
}

export type SessionIntent = 'Arrival' | 'Departure';

export interface Session {
    id?: number;
    activityName: string;
    targetTime: Date;
    intent: SessionIntent;
}

export interface EventRequest {
    name: string;
    startDate: Date;
    endDate: Date;
    graceMinutesBefore: number;
    graceMinutesAfter: number;
    sessions: Session[];
}

export interface EventResponse {
    id: number;
    name: string;
    startDate: string;
    endDate: string;
    graceMinutesBefore: number;
    graceMinutesAfter: number;
    sessions: Session[];
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
    identity: string;
    firstName: string;
    lastName: string;
    attributes?: Record<string, unknown>;
}

export interface AttendeeResponse {
    id: number;
    identity: string;
    firstName: string;
    lastName: string;
    attributes?: Record<string, unknown>;
    createdAt: string;
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
    password: string;
}

export interface Attribute {
    id: number;
    name: string;
    type: 'SELECT';
    options?: string[];
}

export interface AttributeRequest {
    name: string;
    options: string[];
}

export interface AnalyticsBreakdownItem {
    value: string;
    count: number;
}

export interface AnalyticsBreakdownDto {
    attributeName: string;
    breakdown: AnalyticsBreakdownItem[];
}

export interface DashboardStats {
    totalEvents: number;
    totalAttendees: number;
    totalScanners: number;
    liveEntries: number;
}

export interface DailyActivity {
    date: string;
    count: number;
}

export interface Organization {
    id: number;
    name: string;
    identityFormatRegex: string | null;
    lifecycle: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
    subscriptionType: 'LIFETIME' | 'ANNUAL' | 'TRIAL';
    subscriptionExpiresAt: string | null;
}

export interface EntryDetailsDto {
    entryId: number;
    scanTimestamp: string;
    punctuality: 'EARLY' | 'PUNCTUAL' | 'LATE';
    attendee: AttendeeResponse;
}

export interface UpcomingEvent {
    id: number;
    name: string;
    startDate: string;
}

export interface RecentEventStats {
    id: number;
    eventName: string;
    rosterCount: number;
    entryCount: number;
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

export type ImportMode = 'SKIP' | 'UPDATE' | 'FAIL';

export interface ImportConfiguration {
    mode: ImportMode;
    createMissingAttributes: boolean;
    columnMapping: Record<string, string>; // CSV Header -> System Field
}

export interface AttendeeImportAnalysis {
    attendeesToCreate: AttendeeRequest[];
    attendeesToUpdate: AttendeeRequest[];
    invalidRows: InvalidRow[];
    newAttributesToCreate: string[];
}

export interface AttendeeImportCommitRequest {
    attendees: AttendeeRequest[];
    updateExisting: boolean;
    newAttributes: string[];
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
    date: string; // Represents either expiry or creation date
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

export interface Steward {
    id: number;
    email: string;
    createdAt: string;
}

export interface StewardCreateRequest {
    email: string;
    password: string;
}
