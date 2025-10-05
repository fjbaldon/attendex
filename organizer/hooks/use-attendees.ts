import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import api from "@/lib/api";
import {ApiErrorResponse, AttendeeRequest, AttendeeResponse, PaginatedResponse} from "@/types";
import {toast} from "sonner";
import {AxiosError} from "axios";
import {getErrorMessage} from "@/lib/utils";

export const useAttendees = (page = 0, size = 10) => {
    const queryClient = useQueryClient();
    const queryKey = ["attendees", page, size];

    const {data, isLoading: isLoadingAttendees, error: attendeesError} = useQuery<PaginatedResponse<AttendeeResponse>>({
        queryKey,
        queryFn: async () => {
            const response = await api.get("/api/v1/attendees", {
                params: {page, size, sort: "lastName,asc"},
            });
            return response.data;
        },
    });

    const createAttendeeMutation = useMutation<
        AttendeeResponse,
        AxiosError<ApiErrorResponse>,
        AttendeeRequest
    >({
        mutationFn: (newAttendee) => api.post("/api/v1/attendees", newAttendee),
        onSuccess: async () => {
            toast.success("Attendee created successfully!");
            await queryClient.invalidateQueries({queryKey: ["attendees"]});
        },
        onError: (error) => {
            toast.error("Failed to create attendee", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    const updateAttendeeMutation = useMutation<
        AttendeeResponse,
        AxiosError<ApiErrorResponse>,
        { id: number; data: AttendeeRequest }
    >({
        mutationFn: ({id, data}) => api.put(`/api/v1/attendees/${id}`, data),
        onSuccess: async () => {
            toast.success("Attendee updated successfully!");
            await queryClient.invalidateQueries({queryKey: ["attendees"]});
        },
        onError: (error) => {
            toast.error("Failed to update attendee", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    const deleteAttendeeMutation = useMutation<
        void,
        AxiosError<ApiErrorResponse>,
        number
    >({
        mutationFn: (id) => api.delete(`/api/v1/attendees/${id}`),
        onSuccess: async () => {
            toast.success("Attendee deleted successfully!");
            await queryClient.invalidateQueries({queryKey: ["attendees"]});
        },
        onError: (error) => {
            toast.error("Failed to delete attendee", {
                description: getErrorMessage(error, "This attendee may be registered for an event."),
            });
        },
    });

    return {
        attendees: data?.content || [],
        pageInfo: data,
        isLoadingAttendees,
        attendeesError,

        createAttendee: createAttendeeMutation.mutate,
        isCreatingAttendee: createAttendeeMutation.isPending,

        updateAttendee: updateAttendeeMutation.mutate,
        isUpdatingAttendee: updateAttendeeMutation.isPending,

        deleteAttendee: deleteAttendeeMutation.mutate,
        isDeletingAttendee: deleteAttendeeMutation.isPending,
    };
};
