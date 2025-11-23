import {useQuery} from "@tanstack/react-query";
import api from "@/lib/api";
import {DailyActivity, DashboardData} from "@/types";

export const useDashboard = (timeRange: string) => {
    const {data: dashboardData, isLoading: isLoadingDashboardData} = useQuery<DashboardData>({
        queryKey: ["dashboardAll", timeRange],
        queryFn: async () => {
            const response = await api.get("/api/v1/dashboard");
            return response.data;
        },
        refetchInterval: 30000,
        refetchOnWindowFocus: true,
    });

    const activity: DailyActivity[] = [];
    const isLoadingActivity = false;

    return {
        dashboardData,
        isLoading: isLoadingDashboardData || isLoadingActivity,
        activity,
    };
};
