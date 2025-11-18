import {useMutation} from "@tanstack/react-query";
import api from "@/lib/api";
import {ApiErrorResponse} from "@/types";
import {toast} from "sonner";
import {AxiosError} from "axios";
import {getErrorMessage} from "@/lib/utils";

interface ResetPasswordPayload {
    userId: number;
    newTemporaryPassword: string;
}

export const useUserActions = () => {

    const resetPasswordMutation = useMutation<
        void,
        AxiosError<ApiErrorResponse>,
        ResetPasswordPayload
    >({
        mutationFn: ({userId, newTemporaryPassword}) =>
            api.put(`/api/v1/users/${userId}/reset-password`, {newTemporaryPassword}),
        onSuccess: () => {
            toast.success("Password reset successfully!", {
                description: "The user will be required to change this password on their next login.",
            });
        },
        onError: (error) => {
            toast.error("Failed to reset password", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    return {
        resetPassword: resetPasswordMutation.mutate,
        isResettingPassword: resetPasswordMutation.isPending,
    };
};
