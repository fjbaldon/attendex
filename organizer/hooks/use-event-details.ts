import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import api from "@/lib/api";
import {ApiErrorResponse, AttendeeResponse, EventResponse} from "@/types";
import {toast} from "sonner";
import {getErrorMessage} from "@/lib/utils";
import {AxiosError} from "axios";

export const useEventDetails = (eventId: number) => {
    const queryClient = useQueryClient();
    const queryKey = ["eventDetails", eventId];

    const {data: event, isLoading: isLoadingEvent} = useQuery<EventResponse>({
        queryKey: [queryKey, 'details'],
        queryFn: async () => {
            const response = await api.get(`/api/v1/events/${eventId}`);
            return response.data;
        },
    });

    const {data: attendees, isLoading: isLoadingAttendees} = useQuery<AttendeeResponse[]>({
        queryKey: [queryKey, 'attendees'],
        queryFn: async () => {
            const response = await api.get(`/api/v1/events/${eventId}/attendees`);
            return response.data;
        },
    });

    const addAttendeeMutation = useMutation<
        void,
        AxiosError<ApiErrorResponse>,
        { eventId: number; attendeeId: number; }
    >({
        mutationFn: ({eventId, attendeeId}) => api.post(`/api/v1/events/${eventId}/attendees/${attendeeId}`),
        onSuccess: async () => {
            toast.success("Attendee added to event successfully!");
            await queryClient.invalidateQueries({queryKey: [queryKey, 'attendees']});
        },
        onError: (error) => {
            toast.error("Failed to add attendee", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });

    const removeAttendeeMutation = useMutation<
        void,
        AxiosError<ApiErrorResponse>,
        { eventId: number; attendeeId: number; }
    >({
        mutationFn: ({eventId, attendeeId}) => api.delete(`/api/v1/events/${eventId}/attendees/${attendeeId}`),
        onSuccess: async () => {
            toast.success("Attendee removed from event successfully!");
            await queryClient.invalidateQueries({queryKey: [queryKey, 'attendees']});
        },
        onError: (error) => {
            toast.error("Failed to remove attendee", {
                description: getErrorMessage(error, "An unknown error occurred."),
            });
        },
    });


    return {
        event,
        isLoadingEvent,
        attendees: attendees || [],
        isLoadingAttendees,
        addAttendee: addAttendeeMutation.mutate,
        isAddingAttendee: addAttendeeMutation.isPending,
        removeAttendee: removeAttendeeMutation.mutate,
        isRemovingAttendee: removeAttendeeMutation.isPending,
    };
};
