import {keepPreviousData, useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import api from "@/lib/api";
import {ApiErrorResponse, EventRequest, EventResponse, PaginatedResponse} from "@/types";
import {toast} from "sonner";
import {AxiosError} from "axios";
import {getErrorMessage} from "@/lib/utils";

export const useEvents = (page = 0, size = 10, query: string = "") => {
    const queryClient = useQueryClient();
    // Include query in the cache key so specific searches are cached separately
    const queryKey = ["events", page, size, query];

    const {data, isLoading: isLoadingEvents, error: eventsError} = useQuery<PaginatedResponse<EventResponse>>({
        queryKey,
        queryFn: async () => {
            const response = await api.get("/api/v1/events", {
                params: {
                    page,
                    size,
                    sort: "startDate,desc",
                    query: query || undefined // Only send if not empty
                },
            });
            return response.data;
        },
        // FIXED: v5 replacement for keepPreviousData: true
        placeholderData: keepPreviousData,
    });

    const createEventMutation = useMutation<
        EventResponse,
        AxiosError<ApiErrorResponse>,
        EventRequest
    >({
        mutationFn: (newEvent) => api.post("/api/v1/events", newEvent),
        onSuccess: async () => {
            toast.success("Event created successfully!");
            await queryClient.invalidateQueries({queryKey: ["events"]});
        },
        onError: (error) => {
            toast.error("Failed to create event", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    const updateEventMutation = useMutation<
        EventResponse,
        AxiosError<ApiErrorResponse>,
        { id: number; data: EventRequest }
    >({
        mutationFn: ({id, data}) => api.put(`/api/v1/events/${id}`, data),
        onSuccess: async () => {
            toast.success("Event updated successfully!");
            await queryClient.invalidateQueries({queryKey: ["events"]});
        },
        onError: (error) => {
            toast.error("Failed to update event", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    const deleteEventMutation = useMutation<
        void,
        AxiosError<ApiErrorResponse>,
        number
    >({
        mutationFn: (id) => api.delete(`/api/v1/events/${id}`),
        onSuccess: async () => {
            toast.success("Event deleted successfully!");
            await queryClient.invalidateQueries({queryKey: ["events"]});
        },
        onError: (error) => {
            toast.error("Failed to delete event", {
                description: getErrorMessage(error, "This event may have associated data."),
            });
        },
    });

    return {
        eventsData: data,
        isLoadingEvents,
        eventsError,
        createEvent: createEventMutation.mutate,
        isCreatingEvent: createEventMutation.isPending,
        updateEvent: updateEventMutation.mutate,
        isUpdatingEvent: updateEventMutation.isPending,
        deleteEvent: deleteEventMutation.mutate,
        isDeletingEvent: deleteEventMutation.isPending,
    };
};
