import {clsx, type ClassValue} from "clsx";
import {twMerge} from "tailwind-merge";
import {AxiosError} from "axios";
import {ApiErrorResponse} from "@/types";

export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs));
}

export const getErrorMessage = (error: AxiosError<ApiErrorResponse>, defaultMessage: string): string => {
    if (error.response) {
        const errorData = error.response.data;

        if (errorData) {
            if (errorData.validationErrors && Object.keys(errorData.validationErrors).length > 0) {
                return Object.values(errorData.validationErrors)[0];
            }
            if (errorData.message) {
                return errorData.message;
            }
        }
    } else if (error.request) {
        return "Cannot connect to the server. Please check your network connection or contact an administrator.";
    }

    return defaultMessage;
};
