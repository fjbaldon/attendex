import {useQuery} from "@tanstack/react-query";
import api from "@/lib/api";
import {DailyActivity, DashboardData} from "@/types";

export const useDashboard = (timeRange: string) => {
    const {data: dashboardData, isLoading: isLoadingDashboardData} = useQuery<DashboardData>({
        queryKey: ["dashboardAll"],
        queryFn: async () => {
            const response = await api.get("/api/v1/dashboard");
            return response.data;
        },
        // OPTIMIZATION: Reduced polling frequency
        refetchInterval: 60000, // 1 minute
        staleTime: 60000,       // Data is fresh for 1 minute
        refetchOnWindowFocus: false, // Don't refetch just because user clicked window
    });

    const {data: activityData, isLoading: isLoadingActivity} = useQuery<DailyActivity[]>({
        queryKey: ["dashboardActivity", timeRange],
        queryFn: async () => {
            const response = await api.get("/api/v1/dashboard/activity", {
                params: { range: timeRange }
            });
            return response.data;
        },
        refetchInterval: 60000, // 1 minute
        staleTime: 60000,
    });

    return {
        dashboardData,
        isLoading: isLoadingDashboardData || isLoadingActivity,
        activity: activityData || [],
    };
};
