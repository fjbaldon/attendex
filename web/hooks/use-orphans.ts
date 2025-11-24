import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import api from "@/lib/api";
import {ApiErrorResponse, OrphanedEntry, PaginatedResponse} from "@/types";
import {toast} from "sonner";
import {AxiosError} from "axios";
import {getErrorMessage} from "@/lib/utils";

export const useOrphans = (page = 0, size = 10) => {
    const queryClient = useQueryClient();
    const queryKey = ["orphans", page, size];

    const {data, isLoading} = useQuery<PaginatedResponse<OrphanedEntry>>({
        queryKey,
        queryFn: async () => {
            const response = await api.get("/api/v1/capture/orphans", {
                params: {page, size, sort: "createdAt,desc"},
            });
            return response.data;
        },
    });

    const deleteOrphanMutation = useMutation<
        void,
        AxiosError<ApiErrorResponse>,
        number
    >({
        mutationFn: (id) => api.delete(`/api/v1/capture/orphans/${id}`),
        onSuccess: async () => {
            toast.success("Entry dismissed successfully.");
            await queryClient.invalidateQueries({queryKey: ["orphans"]});
        },
        onError: (error) => {
            toast.error("Failed to dismiss entry", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    const deleteOrphansMutation = useMutation<
        void,
        unknown,
        number[]
    >({
        mutationFn: async (ids) => {
            await Promise.all(ids.map(id => api.delete(`/api/v1/capture/orphans/${id}`)));
        },
        onSuccess: async (_, variables) => {
            toast.success(`${variables.length} entries dismissed.`);
            await queryClient.invalidateQueries({queryKey: ["orphans"]});
        },
        onError: () => {
            toast.error("Failed to dismiss some entries.");
            queryClient.invalidateQueries({queryKey: ["orphans"]});
        }
    });

    const recoverOrphanMutation = useMutation<
        void,
        AxiosError<ApiErrorResponse>,
        { orphanId: number; targetEventId: number }
    >({
        mutationFn: ({orphanId, targetEventId}) =>
            api.post(`/api/v1/capture/orphans/${orphanId}/recover?targetEventId=${targetEventId}`),
        onSuccess: async () => {
            toast.success("Entry recovered successfully!");
            await queryClient.invalidateQueries({queryKey: ["orphans"]});
        },
        onError: (error) => {
            toast.error("Failed to recover entry", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    // NEW: Bulk Recover Mutation
    const recoverOrphansMutation = useMutation<
        void,
        unknown,
        { orphanIds: number[]; targetEventId: number }
    >({
        mutationFn: async ({orphanIds, targetEventId}) => {
            await Promise.all(
                orphanIds.map(id =>
                    api.post(`/api/v1/capture/orphans/${id}/recover?targetEventId=${targetEventId}`)
                )
            );
        },
        onSuccess: async (_, variables) => {
            toast.success(`${variables.orphanIds.length} entries recovered!`);
            await queryClient.invalidateQueries({queryKey: ["orphans"]});
        },
        onError: () => {
            toast.error("Failed to recover some entries.");
            queryClient.invalidateQueries({queryKey: ["orphans"]});
        }
    });

    return {
        orphansData: data,
        isLoading,
        deleteOrphan: deleteOrphanMutation.mutate,
        isDeleting: deleteOrphanMutation.isPending,
        deleteOrphans: deleteOrphansMutation.mutate,
        isDeletingMultiple: deleteOrphansMutation.isPending,
        recoverOrphan: recoverOrphanMutation.mutate,
        isRecovering: recoverOrphanMutation.isPending,
        recoverOrphans: recoverOrphansMutation.mutate,
        isRecoveringMultiple: recoverOrphansMutation.isPending,
    };
};
