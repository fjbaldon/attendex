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

interface SearchParams {
    rosterQuery?: string;
    arrivalsQuery?: string;
    departuresQuery?: string;
}

// FIX: Ensure 3rd argument 'search' is defined
export const useEventDetails = (
    eventId: number | null,
    pagination: PageParams,
    search: SearchParams = {}
) => {
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
        queryKey: ["eventDetails", eventId, "roster", pageIndex, pageSize, search.rosterQuery],
        queryFn: async () => {
            if (!eventId) return null;
            const response = await api.get(`/api/v1/events/${eventId}/roster`, {
                params: {
                    page: pageIndex,
                    size: pageSize,
                    query: search.rosterQuery || undefined
                }
            });
            return response.data;
        },
        enabled: !!eventId,
        placeholderData: (prev) => prev,
    });

    const {
        data: arrivalsData,
        isLoading: isLoadingArrivals
    } = useQuery<PaginatedResponse<EntryDetailsDto>>({
        queryKey: ["eventDetails", eventId, "arrivals", pageIndex, pageSize, search.arrivalsQuery],
        queryFn: async () => {
            if (!eventId) return null;
            const response = await api.get(`/api/v1/events/${eventId}/arrivals`, {
                params: {
                    page: pageIndex,
                    size: pageSize,
                    query: search.arrivalsQuery || undefined
                }
            });
            return response.data;
        },
        enabled: !!eventId,
        refetchInterval: REFETCH_INTERVAL_MS,
        placeholderData: (prev) => prev,
    });

    const {
        data: departuresData,
        isLoading: isLoadingDepartures
    } = useQuery<PaginatedResponse<EntryDetailsDto>>({
        queryKey: ["eventDetails", eventId, "departures", pageIndex, pageSize, search.departuresQuery],
        queryFn: async () => {
            if (!eventId) return null;
            const response = await api.get(`/api/v1/events/${eventId}/departures`, {
                params: {
                    page: pageIndex,
                    size: pageSize,
                    query: search.departuresQuery || undefined
                }
            });
            return response.data;
        },
        enabled: !!eventId,
        refetchInterval: REFETCH_INTERVAL_MS,
        placeholderData: (prev) => prev,
    });

    const addAttendeeMutation = useMutation<void, AxiosError<ApiErrorResponse>, {
        eventId: number;
        attendeeId: number;
    }>({
        mutationFn: ({eventId, attendeeId}) => api.post(`/api/v1/events/${eventId}/roster/${attendeeId}`),
        onSuccess: (_, {eventId}) => {
            return queryClient.invalidateQueries({queryKey: ["eventDetails", eventId]});
        },
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

    const removeAttendeesMutation = useMutation<void, unknown, { eventId: number; attendeeIds: number[] }>({
        mutationFn: async ({eventId, attendeeIds}) => {
            await Promise.all(attendeeIds.map(id => api.delete(`/api/v1/events/${eventId}/roster/${id}`)));
        },
        onSuccess: (_, {eventId, attendeeIds}) => {
            toast.success(`${attendeeIds.length} attendees removed from roster.`);
            return queryClient.invalidateQueries({queryKey: ["eventDetails", eventId]});
        },
        onError: () => {
            toast.error("Failed to remove some attendees.");
            if (eventId) void queryClient.invalidateQueries({queryKey: ["eventDetails", eventId]});
        }
    });

    const bulkAddByCriteriaMutation = useMutation<
        { added: number },
        AxiosError<ApiErrorResponse>,
        { eventId: number; criteria: { query: string; attributes: Record<string, string> } }
    >({
        mutationFn: ({eventId, criteria}) =>
            api.post(`/api/v1/events/${eventId}/roster/bulk`, criteria).then(res => res.data),
        onSuccess: (data, {eventId}) => {
            toast.success(`${data.added} attendees added to roster.`);
            return queryClient.invalidateQueries({queryKey: ["eventDetails", eventId]});
        },
        onError: (error) => {
            toast.error("Failed to bulk add attendees", {
                description: getErrorMessage(error, "An unknown error occurred.")
            });
        }
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
        bulkAddByCriteria: bulkAddByCriteriaMutation.mutate,
        isBulkAddingByCriteria: bulkAddByCriteriaMutation.isPending,
        addAttendee: addAttendeeMutation.mutate,
        isAddingAttendee: addAttendeeMutation.isPending,
        removeAttendee: removeAttendeeMutation.mutate,
        isRemovingAttendee: removeAttendeeMutation.isPending,
        removeAttendees: removeAttendeesMutation.mutate,
        isRemovingAttendees: removeAttendeesMutation.isPending,
    };
};
