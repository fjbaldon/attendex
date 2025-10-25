import {z} from "zod";
import {startOfDay, endOfDay} from "date-fns";

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
    eventName: z.string().min(3, "Event name must be at least 3 characters long"),
    startDate: z.date({
        error: "A start date is required.",
    }),
    endDate: z.date({
        error: "An end date is required.",
    }),
    timeSlots: z.array(z.object({
        startTime: z.date(),
        endTime: z.date(),
        type: z.enum(["CHECK_IN", "CHECK_OUT"]),
    })).min(1, "At least one time slot is required.").refine(
        (slots) => slots.every(slot => slot.endTime > slot.startTime),
        {
            message: "End time must be after start time for all slots.",
            path: ["timeSlots"],
        }
    ),
}).refine((data) => data.endDate >= data.startDate, {
    message: "End date cannot be before the start date",
    path: ["endDate"],
}).refine((data) => {
    if (!data.startDate || !data.endDate) return true;
    const eventStart = startOfDay(data.startDate);
    const eventEnd = endOfDay(data.endDate);
    return data.timeSlots.every(slot =>
        slot.startTime >= eventStart && slot.endTime <= eventEnd
    );
}, {
    message: "All time slots must be within the event's start and end dates.",
    path: ["timeSlots"],
});

export const attendeeSchema = z.object({
    uniqueIdentifier: z.string().min(1, "A unique identifier is required."),
    firstName: z.string().min(1, "First name is required."),
    lastName: z.string().min(1, "Last name is required."),
    customFields: z.record(z.string(), z.any()).optional(),
});

export const userCreateSchema = z.object({
    email: z.email("Please enter a valid email address."),
    temporaryPassword: z.string().min(8, "Password must be at least 8 characters long."),
});

export const customFieldSchema = z.object({
    fieldName: z.string().min(2, "Field name must be at least 2 characters."),
    fieldType: z.enum(["TEXT", "NUMBER", "DATE", "SELECT"]),
    options: z.string().optional(),
}).refine(data => {
    if (data.fieldType === 'SELECT') {
        return data.options && data.options.trim().length > 0;
    }
    return true;
}, {
    message: "Options (comma-separated) are required for a Select field.",
    path: ["options"],
});

export const organizationSettingsSchema = z.object({
    name: z.string().min(2, "Organization name must be at least 2 characters long."),
    identifierFormatRegex: z.string().optional(),
});
