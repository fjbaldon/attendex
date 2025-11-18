import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import api from "@/lib/api";
import {toast} from "sonner";
import {AxiosError} from "axios";
import {getErrorMessage} from "@/lib/utils";
import {ApiErrorResponse, Attribute, AttributeRequest} from "@/types";

export const useAttributes = () => {
    const queryClient = useQueryClient();
    const queryKey = ["attributes"];

    const {data: definitions, isLoading} = useQuery<Attribute[]>({
        queryKey,
        queryFn: async () => {
            const response = await api.get("/api/v1/attendees/attributes");
            return response.data;
        },
    });

    const createDefinitionMutation = useMutation<
        Attribute,
        AxiosError<ApiErrorResponse>,
        AttributeRequest
    >({
        mutationFn: (newDefinition) => api.post("/api/v1/attendees/attributes", newDefinition),
        onSuccess: async () => {
            toast.success("Attribute added successfully!");
            await queryClient.invalidateQueries({queryKey});
        },
        onError: (error) => {
            toast.error("Failed to add attribute", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    const updateDefinitionMutation = useMutation<
        Attribute,
        AxiosError<ApiErrorResponse>,
        { attributeId: number; data: AttributeRequest }
    >({
        mutationFn: ({attributeId, data}) => api.put(`/api/v1/attendees/attributes/${attributeId}`, data),
        onSuccess: async () => {
            toast.success("Attribute updated successfully!");
            await queryClient.invalidateQueries({queryKey});
        },
        onError: (error) => {
            toast.error("Failed to update attribute", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    const deleteDefinitionMutation = useMutation<
        void,
        AxiosError<ApiErrorResponse>,
        number
    >({
        mutationFn: (attributeId) => api.delete(`/api/v1/attendees/attributes/${attributeId}`),
        onSuccess: async () => {
            toast.success("Attribute removed successfully!");
            await queryClient.invalidateQueries({queryKey});
        },
        onError: (error) => {
            toast.error("Failed to remove attribute", {
                description: getErrorMessage(error, "The attribute might be in use by an attendee."),
            });
        },
    });

    return {
        definitions: definitions || [],
        isLoading,
        createDefinition: createDefinitionMutation.mutate,
        isCreating: createDefinitionMutation.isPending,
        updateDefinition: updateDefinitionMutation.mutate,
        isUpdating: updateDefinitionMutation.isPending,
        deleteDefinition: deleteDefinitionMutation.mutate,
        isDeleting: deleteDefinitionMutation.isPending,
    };
};
