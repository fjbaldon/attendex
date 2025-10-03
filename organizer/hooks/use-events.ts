import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import api from "@/lib/api";
import {ApiErrorResponse, EventRequest, EventResponse} from "@/types";
import {toast} from "sonner";
import {AxiosError} from "axios";
import {getErrorMessage} from "@/lib/utils";

export const useEvents = () => {
    const queryClient = useQueryClient();

    const {data: events, isLoading: isLoadingEvents, error: eventsError} = useQuery<EventResponse[]>({
        queryKey: ["events"],
        queryFn: async () => {
            const response = await api.get("/api/v1/events");
            return response.data;
        },
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
        events: events || [],
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
