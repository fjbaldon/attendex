import {useQuery} from "@tanstack/react-query";
import api from "@/lib/api";
import {AnalyticsBreakdownDto, EventStats} from "@/types";

export const useAnalytics = (eventId: string | null, attributeName: string | null) => {

    const {data: breakdownData, isLoading: isLoadingBreakdown} = useQuery<AnalyticsBreakdownDto>({
        queryKey: ['analyticsBreakdown', eventId, attributeName],
        queryFn: async () => {
            const response = await api.get(`/api/v1/insights/events/${eventId}/breakdown`, {
                params: {attributeName}
            });
            return response.data;
        },
        enabled: !!eventId && !!attributeName,
    });

    const {data: eventStats, isLoading: isLoadingStats} = useQuery<EventStats>({
        queryKey: ['analyticsStats', eventId],
        queryFn: async () => {
            const response = await api.get(`/api/v1/insights/events/${eventId}/stats`);
            return response.data;
        },
        enabled: !!eventId,
    });

    return {
        breakdown: breakdownData?.breakdown || [],
        stats: eventStats,
        isLoadingBreakdown,
        isLoadingStats
    };
};
