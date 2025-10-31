import {useQuery} from "@tanstack/react-query";
import api from "@/lib/api";
import {AnalyticsBreakdownDto} from "@/types";

export const useAnalytics = (eventId: string, groupBy: string) => {

    const {data: customFields, isLoading: isLoadingCustomFields} = useQuery<string[]>({
        queryKey: ['analyticsCustomFields'],
        queryFn: async () => {
            const response = await api.get('/api/v1/analytics/custom-fields');
            return response.data;
        },
    });

    const {data: breakdownData, isLoading: isLoadingBreakdown} = useQuery<AnalyticsBreakdownDto>({
        queryKey: ['analyticsBreakdown', eventId, groupBy],
        queryFn: async () => {
            const response = await api.get(`/api/v1/analytics/events/${eventId}/breakdown`, {
                params: {groupBy}
            });
            return response.data;
        },
        enabled: !!eventId && !!groupBy,
    });

    return {
        customFields: customFields || [],
        isLoadingCustomFields,
        breakdown: breakdownData?.breakdown || [],
        totalCheckedIn: breakdownData?.totalCheckedIn || 0,
        isLoadingBreakdown,
    };
};
