import {useQuery} from "@tanstack/react-query";
import api from "@/lib/api";
import {EventAnalyticsResponse, EventResponse} from "@/types";

export const useReports = (eventId: number | null) => {
    const {data: events, isLoading: isLoadingEvents} = useQuery<EventResponse[]>({
        queryKey: ['events'],
        queryFn: async () => {
            const response = await api.get('/api/v1/events');
            return response.data;
        }
    });

    const {data: report, isLoading: isLoadingReport} = useQuery<EventAnalyticsResponse>({
        queryKey: ['report', eventId],
        queryFn: async () => {
            const response = await api.get(`/api/v1/reports/events/${eventId}/analytics`);
            return response.data;
        },
        enabled: !!eventId,
    });

    return {
        events: events || [],
        isLoadingEvents,
        report,
        isLoadingReport,
    };
};
