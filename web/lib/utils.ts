import {type ClassValue, clsx} from "clsx";
import {twMerge} from "tailwind-merge";
import {AxiosError} from "axios";
import {ApiErrorResponse} from "@/types";

export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs));
}

export const getErrorMessage = (error: unknown, defaultMessage: string): string => {
    let message = defaultMessage;

    if (error instanceof AxiosError) {
        const errorData = error.response?.data as ApiErrorResponse;

        if (errorData?.validationErrors && Object.keys(errorData.validationErrors).length > 0) {
            message = Object.values(errorData.validationErrors)[0];
        }
        else if (errorData?.message) {
            message = errorData.message;
        }
        else if (!error.response) {
            message = "Cannot connect to the server. Please check your network connection.";
        }
    }
    else if (error instanceof Error) {
        message = error.message;
    }

    return message;
};
