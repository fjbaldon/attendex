import {z} from "zod";

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

export const eventSchema = z.object({
    eventName: z.string().min(3, "Event name must be at least 3 characters long"),
    startDate: z.date({
        error: "A start date is required.",
    }),
    endDate: z.date({
        error: "An end date is required.",
    }),
}).refine((data) => data.endDate >= data.startDate, {
    message: "End date cannot be before the start date",
    path: ["endDate"],
});

export const attendeeSchema = z.object({
    uniqueIdentifier: z.string().min(1, "A unique identifier is required."),
    firstName: z.string().min(1, "First name is required."),
    lastName: z.string().min(1, "Last name is required."),
    customFields: z.string().optional().transform((val, ctx) => {
        if (!val) return {};
        try {
            return JSON.parse(val);
        } catch {
            ctx.addIssue({
                code: "custom",
                message: "Invalid JSON format for custom fields.",
            });
            return z.NEVER;
        }
    }),
});
