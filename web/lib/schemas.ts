import {z} from "zod";
import {endOfDay, startOfDay} from "date-fns";

export const loginSchema = z.object({
    email: z.email("Please enter a valid email address"),
    password: z.string().min(8, "Password must be at least 8 characters long"),
});

export const registerSchema = z.object({
    organizationName: z.string().min(2, "Organization name must be at least 2 characters long"),
    email: z.email("Please enter a valid email address"),
    password: z.string().min(8, "Password must be at least 8 characters long"),
});

export const passwordChangeSchema = z.object({
    newPassword: z.string().min(8, "Password must be at least 8 characters long"),
    confirmPassword: z.string(),
}).refine((data) => data.newPassword === data.confirmPassword, {
    message: "Passwords do not match",
    path: ["confirmPassword"],
});

export const passwordResetSchema = z.object({
    newTemporaryPassword: z.string().min(8, "Password must be at least 8 characters long"),
    confirmPassword: z.string(),
}).refine((data) => data.newTemporaryPassword === data.confirmPassword, {
    message: "Passwords do not match",
    path: ["confirmPassword"],
});

export const eventSchema = z.object({
    name: z.string().min(3, "Event name must be at least 3 characters long"),
    startDate: z.date({
        error: "A start date is required.",
    }),
    endDate: z.date({
        error: "An end date is required.",
    }),
    graceMinutesBefore: z.preprocess(
        (val) => Number(val),
        z.number().min(0, "Must be 0 or greater.")
    ),
    graceMinutesAfter: z.preprocess(
        (val) => Number(val),
        z.number().min(0, "Must be 0 or greater.")
    ),
    sessions: z.array(z.object({
        id: z.number().optional(),
        activityName: z.string().min(1, "Activity name is required."),
        targetTime: z.date(),
        intent: z.enum(["Arrival", "Departure"]),
    })).min(1, "At least one session is required."),
}).refine((data) => {
    if (!data.startDate || !data.endDate) return true;
    return startOfDay(data.endDate) >= startOfDay(data.startDate);
}, {
    message: "End date cannot be before the start date",
    path: ["endDate"],
}).refine((data) => {
    if (!data.startDate || !data.endDate || !data.sessions.length) return true;
    const eventStart = startOfDay(data.startDate);
    const eventEnd = endOfDay(data.endDate);
    return data.sessions.every(slot => {
        const targetTime = slot.targetTime;
        return targetTime >= eventStart && targetTime <= eventEnd;
    });
}, {
    message: "All session times must fall within the event's start and end dates",
    path: ["sessions"],
});

export const attendeeSchema = z.object({
    identity: z.string().min(1, "An identifier is required."),
    firstName: z.string().min(1, "First name is required."),
    lastName: z.string().min(1, "Last name is required."),
    attributes: z.record(z.string(), z.unknown()).optional(),
});

export const userCreateSchema = z.object({
    email: z.email("Please enter a valid email address."),
    password: z.string().min(8, "Password must be at least 8 characters long."),
});

export const attributeSchema = z.object({
    name: z.string().min(2, "Attribute name must be at least 2 characters."),
    options: z.string().min(1, "Options (comma-separated) are required."),
});

export const organizationSettingsSchema = z.object({
    name: z.string().min(2, "Organization name must be at least 2 characters long."),
    identityFormatRegex: z.string().optional(),
});
