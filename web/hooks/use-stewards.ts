import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import api from "@/lib/api";
import {ApiErrorResponse, PaginatedResponse, Steward, StewardCreateRequest} from "@/types";
import {toast} from "sonner";
import {getErrorMessage} from "@/lib/utils";
import {AxiosError} from "axios";

// FIX: Added 'query' parameter (3rd argument)
export const useStewards = (page = 0, size = 10, query = "") => {
    const queryClient = useQueryClient();
    const queryKey = ["stewards", page, size, query];

    const {data, isLoading: isLoadingStewards} = useQuery<PaginatedResponse<Steward>>({
        queryKey,
        queryFn: async () => {
            const response = await api.get("/api/v1/admin/stewards", {
                params: {
                    page,
                    size,
                    sort: "createdAt,desc",
                    ...(query && { query })
                },
            });
            return response.data;
        },
        placeholderData: (prev) => prev,
    });

    const createStewardMutation = useMutation<
        Steward,
        AxiosError<ApiErrorResponse>,
        StewardCreateRequest
    >({
        mutationFn: (newSteward) => api.post("/api/v1/admin/stewards", newSteward),
        onSuccess: async () => {
            toast.success("Steward created successfully!");
            await queryClient.invalidateQueries({queryKey: ["stewards"]});
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
            await queryClient.invalidateQueries({queryKey: ["stewards"]});
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
        resetStewardPassword: resetStewardPasswordMutation.mutate,
        isResettingStewardPassword: resetStewardPasswordMutation.isPending,
    };
};
