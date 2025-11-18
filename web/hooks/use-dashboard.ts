import {useQuery} from "@tanstack/react-query";
import api from "@/lib/api";
import {DailyActivity, DashboardData} from "@/types";

export const useDashboard = (timeRange: string) => {
    const {data: dashboardData, isLoading: isLoadingDashboardData} = useQuery<DashboardData>({
        queryKey: ["dashboardAll"],
        queryFn: async () => {
            const response = await api.get("/api/v1/dashboard/all");
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
        dashboardData,
        isLoading: isLoadingDashboardData || isLoadingActivity,
        activity: activity || [],
    };
};
