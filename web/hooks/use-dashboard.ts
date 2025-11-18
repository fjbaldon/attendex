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
    });

    // The activity data is no longer fetched separately in the platform backend.
    // For now, we return an empty array to satisfy the component props.
    // A future enhancement could be a dedicated activity endpoint if needed.
    const activity: DailyActivity[] = [];
    const isLoadingActivity = false;

    return {
        dashboardData,
        isLoading: isLoadingDashboardData || isLoadingActivity,
        activity,
    };
};
