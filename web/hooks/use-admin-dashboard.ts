import {useQuery} from "@tanstack/react-query";
import api from "@/lib/api";
import {AdminDashboardData, DailyRegistration} from "@/types";

export const useAdminDashboard = (timeRange: string) => {
    const {data: dashboardData, isLoading: isLoadingDashboard} = useQuery<AdminDashboardData>({
        queryKey: ["adminDashboard"],
        queryFn: async () => {
            const response = await api.get("/api/v1/admin/dashboard");
            return response.data;
        },
    });

    const {data: activity, isLoading: isLoadingActivity} = useQuery<DailyRegistration[]>({
        queryKey: ["adminDashboardActivity", timeRange],
        queryFn: async () => {
            const response = await api.get("/api/v1/admin/dashboard/registrations", {
                params: {range: timeRange},
            });
            return response.data;
        },
    });

    return {
        dashboardData,
        activity: activity || [],
        isLoading: isLoadingDashboard || isLoadingActivity,
    };
};
