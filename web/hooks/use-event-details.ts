import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import api from "@/lib/api";
import {ApiErrorResponse, AttendeeResponse, EntryDetailsDto, EventResponse, PaginatedResponse} from "@/types";
import {toast} from "sonner";
import {getErrorMessage} from "@/lib/utils";
import {AxiosError} from "axios";

const REFETCH_INTERVAL_MS = 20000;

interface PageParams {
    pageIndex: number;
    pageSize: number;
}

export const useEventDetails = (eventId: number | null, pagination: PageParams) => {
    const queryClient = useQueryClient();
    const {pageIndex, pageSize} = pagination;

    const {data: event, isLoading: isLoadingEvent} = useQuery<EventResponse>({
        queryKey: ["eventDetails", eventId],
        queryFn: async () => {
            if (!eventId) return null;
            const response = await api.get(`/api/v1/events/${eventId}`);
            return response.data;
        },
        enabled: !!eventId,
    });

    const {data: attendeesData, isLoading: isLoadingAttendees} = useQuery<PaginatedResponse<AttendeeResponse>>({
        queryKey: ["eventDetails", eventId, "roster", pageIndex, pageSize],
        queryFn: async () => {
            if (!eventId) return null;
            const response = await api.get(`/api/v1/events/${eventId}/roster`, {
                params: {page: pageIndex, size: pageSize}
            });
            return response.data;
        },
        enabled: !!eventId,
    });

    const {
        data: arrivalsData,
        isLoading: isLoadingArrivals
    } = useQuery<PaginatedResponse<EntryDetailsDto>>({
        queryKey: ["eventDetails", eventId, "arrivals", pageIndex, pageSize],
        queryFn: async () => {
            if (!eventId) return null;
            const response = await api.get(`/api/v1/events/${eventId}/arrivals`, {
                params: {page: pageIndex, size: pageSize}
            });
            return response.data;
        },
        enabled: !!eventId,
        refetchInterval: REFETCH_INTERVAL_MS,
    });

    const {
        data: departuresData,
        isLoading: isLoadingDepartures
    } = useQuery<PaginatedResponse<EntryDetailsDto>>({
        queryKey: ["eventDetails", eventId, "departures", pageIndex, pageSize],
        queryFn: async () => {
            if (!eventId) return null;
            const response = await api.get(`/api/v1/events/${eventId}/departures`, {
                params: {page: pageIndex, size: pageSize}
            });
            return response.data;
        },
        enabled: !!eventId,
        refetchInterval: REFETCH_INTERVAL_MS,
    });


    const addAttendeeMutation = useMutation<void, AxiosError<ApiErrorResponse>, {
        eventId: number;
        attendeeId: number;
    }>({
        mutationFn: ({eventId, attendeeId}) => api.post(`/api/v1/events/${eventId}/roster/${attendeeId}`),
        onSuccess: (_, {eventId}) => {
            toast.success("Attendee added to roster successfully!");
            return queryClient.invalidateQueries({queryKey: ["eventDetails", eventId]});
        },
        onError: (error) => toast.error("Failed to add attendee", {description: getErrorMessage(error, "An unknown error occurred.")}),
    });

    const removeAttendeeMutation = useMutation<void, AxiosError<ApiErrorResponse>, {
        eventId: number;
        attendeeId: number;
    }>({
        mutationFn: ({eventId, attendeeId}) => api.delete(`/api/v1/events/${eventId}/roster/${attendeeId}`),
        onSuccess: (_, {eventId}) => {
            toast.success("Attendee removed from roster successfully!");
            return queryClient.invalidateQueries({queryKey: ["eventDetails", eventId]});
        },
        onError: (error) => toast.error("Failed to remove attendee", {description: getErrorMessage(error, "An unknown error occurred.")}),
    });

    return {
        event,
        isLoadingEvent,
        attendeesData,
        isLoadingAttendees,
        arrivalsData,
        isLoadingArrivals,
        departuresData,
        isLoadingDepartures,
        addAttendee: addAttendeeMutation.mutate,
        isAddingAttendee: addAttendeeMutation.isPending,
        removeAttendee: removeAttendeeMutation.mutate,
        isRemovingAttendee: removeAttendeeMutation.isPending,
    };
};
