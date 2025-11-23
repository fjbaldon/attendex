import {useQuery} from "@tanstack/react-query";
import api from "@/lib/api";
import {AnalyticsBreakdownDto} from "@/types";

export const useAnalytics = (eventId: string | null, attributeName: string | null) => {

    const {data: breakdownData, isLoading: isLoadingBreakdown} = useQuery<AnalyticsBreakdownDto>({
        queryKey: ['analyticsBreakdown', eventId, attributeName],
        queryFn: async () => {
            const response = await api.get(`/api/v1/analytics/events/${eventId}/breakdown`, {
                params: {attributeName}
            });
            return response.data;
        },
        enabled: !!eventId && !!attributeName,
    });

    return {
        breakdown: breakdownData?.breakdown || [],
        isLoadingBreakdown,
    };
};
