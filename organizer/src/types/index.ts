export interface Event {
    id: number;
    eventName: string;
    startDate: string;
    endDate: string;
}

export interface Attendee {
    id: number;
    schoolIdNumber: string;
    firstName: string;
    middleInitial?: string;
    lastName: string;
    course?: string;
    yearLevel?: number;
}

export interface Scanner {
    id: number;
    username: string;
}

export interface Page<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    number: number;
    size: number;
}

export interface EventAnalytics {
    eventId: number;
    eventName: string;
    totalRegistered: number;
    totalCheckedIn: number;
    attendanceRate: number;
    checkInsByDate: Record<string, number>;
}
