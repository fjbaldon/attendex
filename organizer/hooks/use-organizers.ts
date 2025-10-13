import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import api from "@/lib/api";
import {ApiErrorResponse, OrganizerResponse, PaginatedResponse, UserCreateRequest} from "@/types";
import {toast} from "sonner";
import {AxiosError} from "axios";
import {getErrorMessage} from "@/lib/utils";

type OrganizerCreateRequest = Omit<UserCreateRequest, 'roleId'>;

export const useOrganizers = (page = 0, size = 10) => {
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

    const createOrganizerMutation = useMutation<
        OrganizerResponse,
        AxiosError<ApiErrorResponse>,
        OrganizerCreateRequest
    >({
        mutationFn: (newOrganizerData) => api.post("/api/v1/organizers", newOrganizerData),
        onSuccess: async () => {
            toast.success("Organizer created successfully!");
            await queryClient.invalidateQueries({queryKey});
        },
        onError: (error) => {
            toast.error("Failed to create organizer", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    const deleteOrganizerMutation = useMutation<
        void,
        AxiosError<ApiErrorResponse>,
        number
    >({
        mutationFn: (id) => api.delete(`/api/v1/organizers/${id}`),
        onSuccess: async () => {
            toast.success("Organizer removed successfully!");
            await queryClient.invalidateQueries({queryKey});
        },
        onError: (error) => {
            toast.error("Failed to remove organizer", {
                description: getErrorMessage(error, "You may not be able to remove the last organizer or an organizer who owns events."),
            });
        },
    });

    return {
        organizers: data?.content || [],
        pageInfo: data,
        isLoadingOrganizers,
        createOrganizer: createOrganizerMutation.mutate,
        isCreatingOrganizer: createOrganizerMutation.isPending,
        deleteOrganizer: deleteOrganizerMutation.mutate,
        isDeletingOrganizer: deleteOrganizerMutation.isPending,
    };
};
