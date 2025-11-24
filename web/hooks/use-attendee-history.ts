import {useQuery} from "@tanstack/react-query";
import api from "@/lib/api";
import {AttendeeHistory} from "@/types";

export const useAttendeeHistory = (attendeeId: number | null) => {
    const {data, isLoading, error} = useQuery<AttendeeHistory>({
        queryKey: ["attendeeHistory", attendeeId],
        queryFn: async () => {
            if (!attendeeId) return null;
            // Note: Updated path to match AnalyticsController
            const response = await api.get(`/api/v1/insights/attendees/${attendeeId}/history`);
            return response.data;
        },
        enabled: !!attendeeId,
    });

    return {
        history: data,
        isLoading,
        error
    };
};
