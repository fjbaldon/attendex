import {useQuery} from "@tanstack/react-query";
import api from "@/lib/api";
import {DashboardStats, DailyActivity} from "@/types";

export const useDashboard = (timeRange: string) => {
    const {data: stats, isLoading: isLoadingStats} = useQuery<DashboardStats>({
        queryKey: ["dashboardStats"],
        queryFn: async () => {
            const response = await api.get("/api/v1/dashboard/stats");
            return response.data;
        },
    });

    const {data: activity, isLoading: isLoadingActivity} = useQuery<DailyActivity[]>({
        queryKey: ["dashboardActivity", timeRange],
        queryFn: async () => {
            const response = await api.get("/api/v1/dashboard/activity", {
                params: {range: timeRange}
            });
            return response.data;
        },
    });

    return {
        stats,
        isLoadingStats,
        activity: activity || [],
        isLoadingActivity,
    };
};
