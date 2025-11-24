import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import api from "@/lib/api";
import {ApiErrorResponse, Organization, PaginatedResponse} from "@/types";
import {toast} from "sonner";
import {getErrorMessage} from "@/lib/utils";
import {AxiosError} from "axios";

type OrganizationStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
type SubscriptionType = 'LIFETIME' | 'ANNUAL' | 'TRIAL';

interface OrganizationLifecycleUpdate {
    lifecycle: OrganizationStatus;
}

interface OrganizationSubscriptionUpdate {
    subscriptionType: SubscriptionType;
    expiresAt: Date | null;
}

export const useAdminOrganizations = (
    page = 0,
    size = 10,
    query = "",
    lifecycle = "ALL",
    subscriptionType = "ALL"
) => {
    const queryClient = useQueryClient();
    // Include filters in query key for caching
    const queryKey = ["adminOrganizations", page, size, query, lifecycle, subscriptionType];

    const {data, isLoading: isLoadingOrganizations} = useQuery<PaginatedResponse<Organization>>({
        queryKey,
        queryFn: async () => {
            const params: Record<string, string | number> = {
                page,
                size,
                sort: "name,asc",
            };

            if (query) params.query = query;
            if (lifecycle && lifecycle !== "ALL") params.lifecycle = lifecycle;
            if (subscriptionType && subscriptionType !== "ALL") params.subscriptionType = subscriptionType;

            const response = await api.get("/api/v1/admin/organizations", { params });
            return response.data;
        },
        placeholderData: (prev) => prev,
    });

    const updateStatusMutation = useMutation<
        Organization,
        AxiosError<ApiErrorResponse>,
        { id: number; data: OrganizationLifecycleUpdate }
    >({
        mutationFn: ({id, data}) => api.put(`/api/v1/admin/organizations/${id}/status`, data),
        onSuccess: async () => {
            toast.success("Organization status updated successfully!");
            await queryClient.invalidateQueries({queryKey: ["adminOrganizations"]});
        },
        onError: (error) => {
            toast.error("Failed to update status", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    const updateSubscriptionMutation = useMutation<
        Organization,
        AxiosError<ApiErrorResponse>,
        { id: number; data: OrganizationSubscriptionUpdate }
    >({
        mutationFn: ({id, data}) => api.put(`/api/v1/admin/organizations/${id}/subscription`, data),
        onSuccess: async () => {
            toast.success("Organization subscription updated successfully!");
            await queryClient.invalidateQueries({queryKey: ["adminOrganizations"]});
        },
        onError: (error) => {
            toast.error("Failed to update subscription", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    return {
        organizationsData: data,
        isLoadingOrganizations,
        updateStatus: updateStatusMutation.mutate,
        isUpdatingStatus: updateStatusMutation.isPending,
        updateSubscription: updateSubscriptionMutation.mutate,
        isUpdatingSubscription: updateSubscriptionMutation.isPending,
    };
};
