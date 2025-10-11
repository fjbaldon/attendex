import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import api from "@/lib/api";
import {toast} from "sonner";
import {AxiosError} from "axios";
import {getErrorMessage} from "@/lib/utils";
import {ApiErrorResponse, CustomFieldDefinition, CustomFieldDefinitionRequest} from "@/types";

export const useCustomFields = () => {
    const queryClient = useQueryClient();
    const queryKey = ["customFieldDefinitions"];

    const {data: definitions, isLoading} = useQuery<CustomFieldDefinition[]>({
        queryKey,
        queryFn: async () => {
            const response = await api.get("/api/v1/custom-fields");
            return response.data;
        },
    });

    const createDefinitionMutation = useMutation<
        CustomFieldDefinition,
        AxiosError<ApiErrorResponse>,
        CustomFieldDefinitionRequest
    >({
        mutationFn: (newDefinition) => api.post("/api/v1/custom-fields", newDefinition),
        onSuccess: async () => {
            toast.success("Custom field added successfully!");
            await queryClient.invalidateQueries({queryKey});
        },
        onError: (error) => {
            toast.error("Failed to add field", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    const deleteDefinitionMutation = useMutation<
        void,
        AxiosError<ApiErrorResponse>,
        number
    >({
        mutationFn: (fieldId) => api.delete(`/api/v1/custom-fields/${fieldId}`),
        onSuccess: async () => {
            toast.success("Custom field removed successfully!");
            await queryClient.invalidateQueries({queryKey});
        },
        onError: (error) => {
            toast.error("Failed to remove field", {
                description: getErrorMessage(error, "The field might be in use by an attendee."),
            });
        },
    });

    return {
        definitions: definitions || [],
        isLoading,
        createDefinition: createDefinitionMutation.mutate,
        isCreating: createDefinitionMutation.isPending,
        deleteDefinition: deleteDefinitionMutation.mutate,
        isDeleting: deleteDefinitionMutation.isPending,
    };
};
