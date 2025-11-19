import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import api from "@/lib/api";
import {ApiErrorResponse, Organization} from "@/types";
import {toast} from "sonner";
import {AxiosError} from "axios";
import {getErrorMessage} from "@/lib/utils";

interface OrganizationUpdateRequest {
    name: string;
    identityFormatRegex: string | null;
}

export const useOrganization = () => {
    const queryClient = useQueryClient();
    const queryKey = ["organization"];

    const {data, isLoading} = useQuery<Organization>({
        queryKey,
        queryFn: async () => {
            const response = await api.get('/api/v1/organization');
            return response.data;
        }
    });

    const updateOrganizationMutation = useMutation<
        Organization,
        AxiosError<ApiErrorResponse>,
        OrganizationUpdateRequest
    >({
        mutationFn: (updateData) => api.put('/api/v1/organization', updateData),
        onSuccess: async () => {
            toast.success("Organization settings saved successfully!");
            await queryClient.invalidateQueries({queryKey});
        },
        onError: (error) => {
            toast.error("Failed to save settings", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        }
    });

    return {
        organization: data,
        isLoading,
        updateOrganization: updateOrganizationMutation.mutate,
        isUpdating: updateOrganizationMutation.isPending,
    };
};
