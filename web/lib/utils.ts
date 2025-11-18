import {clsx, type ClassValue} from "clsx";
import {twMerge} from "tailwind-merge";
import {AxiosError} from "axios";
import {ApiErrorResponse} from "@/types";

export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs));
}

export const getErrorMessage = (error: unknown, defaultMessage: string): string => {
    if (error instanceof AxiosError) {
        if (error.response?.data) {
            const errorData = error.response.data as ApiErrorResponse;
            if (errorData.validationErrors && Object.keys(errorData.validationErrors).length > 0) {
                return Object.values(errorData.validationErrors)[0];
            }
            if (errorData.message) {
                return errorData.message;
            }
        }
        if (error.request) {
            return "Cannot connect to the server. Please check your network connection.";
        }
    }

    if (error instanceof Error) {
        return error.message;
    }

    return defaultMessage;
};
