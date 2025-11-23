import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import api from "@/lib/api";
import {ApiErrorResponse, PaginatedResponse, Steward, StewardCreateRequest} from "@/types";
import {toast} from "sonner";
import {getErrorMessage} from "@/lib/utils";
import {AxiosError} from "axios";

export const useStewards = (page = 0, size = 10) => {
    const queryClient = useQueryClient();
    const queryKey = ["stewards", page, size];

    const {data, isLoading: isLoadingStewards} = useQuery<PaginatedResponse<Steward>>({
        queryKey,
        queryFn: async () => {
            const response = await api.get("/api/v1/admin/stewards", {
                params: {page, size, sort: "createdAt,desc"},
            });
            return response.data;
        },
    });

    const createStewardMutation = useMutation<
        Steward,
        AxiosError<ApiErrorResponse>,
        StewardCreateRequest
    >({
        mutationFn: (newSteward) => api.post("/api/v1/admin/stewards", newSteward),
        onSuccess: async () => {
            toast.success("Steward created successfully!");
            await queryClient.invalidateQueries({queryKey});
        },
        onError: (error) => {
            toast.error("Failed to create steward", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    const deleteStewardMutation = useMutation<
        void,
        AxiosError<ApiErrorResponse>,
        number
    >({
        mutationFn: (id) => api.delete(`/api/v1/admin/stewards/${id}`),
        onSuccess: async () => {
            toast.success("Steward deleted successfully!");
            await queryClient.invalidateQueries({queryKey});
        },
        onError: (error) => {
            toast.error("Failed to delete steward", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    const resetStewardPasswordMutation = useMutation<
        void,
        AxiosError<ApiErrorResponse>,
        { id: number; newPassword: string }
    >({
        mutationFn: ({id, newPassword}) =>
            api.put(`/api/v1/admin/stewards/${id}/reset-password`, { newPassword }),
        onSuccess: () => {
            toast.success("Steward password reset successfully.");
        },
        onError: (error) => {
            toast.error("Failed to reset password", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    return {
        stewardsData: data,
        isLoadingStewards,
        createSteward: createStewardMutation.mutate,
        isCreatingSteward: createStewardMutation.isPending,
        deleteSteward: deleteStewardMutation.mutate,
        isDeletingSteward: deleteStewardMutation.isPending,
        // Export new capability
        resetStewardPassword: resetStewardPasswordMutation.mutate,
        isResettingStewardPassword: resetStewardPasswordMutation.isPending,
    };
};
