import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import api from "@/lib/api";
import {
    ApiErrorResponse,
    OrganizerResponse,
    OrganizerRoleUpdateRequest, PaginatedResponse,
    RoleResponse,
    UserCreateRequest
} from "@/types";
import {toast} from "sonner";
import {AxiosError} from "axios";
import {getErrorMessage} from "@/lib/utils";

export const useUsers = (page = 0, size = 10) => {
    const queryClient = useQueryClient();
    const queryKey = ["organizers", page, size];

    const {data, isLoading: isLoadingOrganizers} = useQuery<PaginatedResponse<OrganizerResponse>>({
        queryKey,
        queryFn: async () => {
            const response = await api.get("/api/v1/organizers", {
                params: {page, size, sort: "email,asc"}
            });
            return response.data;
        },
    });

    const {data: roles, isLoading: isLoadingRoles} = useQuery<RoleResponse[]>({
        queryKey: ["roles"],
        queryFn: async () => {
            const response = await api.get("/api/v1/roles");
            return response.data;
        },
    });

    const createUserMutation = useMutation<
        void,
        AxiosError<ApiErrorResponse>,
        UserCreateRequest
    >({
        mutationFn: (newUserData) => api.post("/api/v1/users", newUserData),
        onSuccess: async () => {
            toast.success("User created successfully!", {
                description: "They can now log in with the temporary password you provided.",
            });
            await queryClient.invalidateQueries({queryKey: ["organizers"]});
        },
        onError: (error) => {
            toast.error("Failed to create user", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    const updateUserRoleMutation = useMutation<
        OrganizerResponse,
        AxiosError<ApiErrorResponse>,
        { id: number; data: OrganizerRoleUpdateRequest }
    >({
        mutationFn: ({id, data}) => api.put(`/api/v1/organizers/${id}/role`, data),
        onSuccess: async () => {
            toast.success("User role updated successfully!");
            await queryClient.invalidateQueries({queryKey: ["organizers"]});
        },
        onError: (error) => {
            toast.error("Failed to update role", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    const deleteUserMutation = useMutation<
        void,
        AxiosError<ApiErrorResponse>,
        number
    >({
        mutationFn: (id) => api.delete(`/api/v1/organizers/${id}`),
        onSuccess: async () => {
            toast.success("User removed successfully!");
            await queryClient.invalidateQueries({queryKey: ["organizers"]});
        },
        onError: (error) => {
            toast.error("Failed to remove user", {
                description: getErrorMessage(error, "You may not be able to remove the last admin or an admin who owns events."),
            });
        },
    });

    return {
        organizers: data?.content || [],
        pageInfo: data,
        isLoadingOrganizers,
        roles: roles || [],
        isLoadingRoles,

        createUser: createUserMutation.mutate,
        isCreatingUser: createUserMutation.isPending,

        updateUserRole: updateUserRoleMutation.mutate,
        isUpdatingUserRole: updateUserRoleMutation.isPending,

        deleteUser: deleteUserMutation.mutate,
        isDeletingUser: deleteUserMutation.isPending,
    };
};
