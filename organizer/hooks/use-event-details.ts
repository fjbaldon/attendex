import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import api from "@/lib/api";
import {ApiErrorResponse, AttendeeResponse, CheckedInAttendeeResponse, EventResponse} from "@/types";
import {toast} from "sonner";
import {getErrorMessage} from "@/lib/utils";
import {AxiosError} from "axios";

export const useEventDetails = (eventId: number | null) => {
    const queryClient = useQueryClient();

    const {data: event, isLoading: isLoadingEvent} = useQuery<EventResponse>({
        queryKey: ["eventDetails", eventId, "details"],
        queryFn: async () => {
            if (!eventId) return null;
            const response = await api.get(`/api/v1/events/${eventId}`);
            return response.data;
        },
        enabled: !!eventId,
    });

    const {data: attendees, isLoading: isLoadingAttendees} = useQuery<AttendeeResponse[]>({
        queryKey: ["eventDetails", eventId, "attendees"],
        queryFn: async () => {
            if (!eventId) return [];
            const response = await api.get(`/api/v1/events/${eventId}/attendees`);
            return response.data;
        },
        enabled: !!eventId,
    });

    const {data: checkedInAttendees, isLoading: isLoadingCheckedIn} = useQuery<CheckedInAttendeeResponse[]>({
        queryKey: ["eventDetails", eventId, "checkedIn"],
        queryFn: async () => {
            if (!eventId) return [];
            const response = await api.get(`/api/v1/events/${eventId}/checked-in`);
            return response.data;
        },
        enabled: !!eventId,
    });

    const addAttendeeMutation = useMutation<void, AxiosError<ApiErrorResponse>, {
        eventId: number;
        attendeeId: number;
    }>({
        mutationFn: ({eventId, attendeeId}) => api.post(`/api/v1/events/${eventId}/attendees/${attendeeId}`),
        onSuccess: (_, {eventId}) => {
            toast.success("Attendee added to event successfully!");
            return queryClient.invalidateQueries({queryKey: ["eventDetails", eventId, "attendees"]});
        },
        onError: (error) => toast.error("Failed to add attendee", {description: getErrorMessage(error, "An unknown error occurred.")}),
    });

    const removeAttendeeMutation = useMutation<void, AxiosError<ApiErrorResponse>, {
        eventId: number;
        attendeeId: number;
    }>({
        mutationFn: ({eventId, attendeeId}) => api.delete(`/api/v1/events/${eventId}/attendees/${attendeeId}`),
        onSuccess: (_, {eventId}) => {
            toast.success("Attendee removed from event successfully!");
            return queryClient.invalidateQueries({queryKey: ["eventDetails", eventId, "attendees"]});
        },
        onError: (error) => toast.error("Failed to remove attendee", {description: getErrorMessage(error, "An unknown error occurred.")}),
    });

    return {
        event,
        isLoadingEvent,
        attendees: attendees || [],
        isLoadingAttendees,
        checkedInAttendees: checkedInAttendees || [],
        isLoadingCheckedIn,
        addAttendee: addAttendeeMutation.mutate,
        isAddingAttendee: addAttendeeMutation.isPending,
        removeAttendee: removeAttendeeMutation.mutate,
        isRemovingAttendee: removeAttendeeMutation.isPending,
    };
};
