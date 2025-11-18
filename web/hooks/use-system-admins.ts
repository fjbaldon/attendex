import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import api from "@/lib/api";
import {ApiErrorResponse, PaginatedResponse, SystemAdmin, SystemAdminCreateRequest} from "@/types";
import {toast} from "sonner";
import {getErrorMessage} from "@/lib/utils";
import {AxiosError} from "axios";

export const useSystemAdmins = (page = 0, size = 10) => {
    const queryClient = useQueryClient();
    const queryKey = ["systemAdmins", page, size];

    const {data, isLoading: isLoadingAdmins} = useQuery<PaginatedResponse<SystemAdmin>>({
        queryKey,
        queryFn: async () => {
            const response = await api.get("/api/v1/admin/system-admins", {
                params: {page, size, sort: "createdAt,desc"},
            });
            return response.data;
        },
    });

    const createAdminMutation = useMutation<
        SystemAdmin,
        AxiosError<ApiErrorResponse>,
        SystemAdminCreateRequest
    >({
        mutationFn: (newAdmin) => api.post("/api/v1/admin/system-admins", newAdmin),
        onSuccess: async () => {
            toast.success("System admin created successfully!");
            await queryClient.invalidateQueries({queryKey});
        },
        onError: (error) => {
            toast.error("Failed to create admin", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    const deleteAdminMutation = useMutation<
        void,
        AxiosError<ApiErrorResponse>,
        number
    >({
        mutationFn: (id) => api.delete(`/api/v1/admin/system-admins/${id}`),
        onSuccess: async () => {
            toast.success("System admin deleted successfully!");
            await queryClient.invalidateQueries({queryKey});
        },
        onError: (error) => {
            toast.error("Failed to delete admin", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    return {
        adminsData: data,
        isLoadingAdmins,
        createAdmin: createAdminMutation.mutate,
        isCreatingAdmin: createAdminMutation.isPending,
        deleteAdmin: deleteAdminMutation.mutate,
        isDeletingAdmin: deleteAdminMutation.isPending,
    };
};
