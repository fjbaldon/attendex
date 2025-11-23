"use client";

import * as React from "react";
import {useAdminDashboard} from "@/hooks/use-admin-dashboard";
import {AdminStatsCards} from "./admin-stats-cards";
import {ExpiringSubscriptions} from "./expiring-subscriptions";
import {RecentRegistrations} from "./recent-registrations";
import {SiteHeader} from "@/components/layout/site-header";
import {AdminChartAreaInteractive} from "./admin-chart-area-interactive";
import {AttentionRequired} from "./attention-required";

export default function AdminDashboardPage() {
    const [timeRange, setTimeRange] = React.useState("90d");
    const {dashboardData, activity, isLoading} = useAdminDashboard(timeRange);

    return (
        <>
            <SiteHeader title="Dashboard"/>
            <main className="@container/main flex-1 p-4 lg:p-6">
                <div className="w-full max-w-6xl mx-auto">
                    <div className="flex flex-col gap-6">
                        <AdminStatsCards stats={dashboardData?.stats} isLoading={isLoading}/>
                        <AdminChartAreaInteractive
                            data={activity}
                            isLoading={isLoading}
                            timeRange={timeRange}
                            setTimeRange={setTimeRange}
                        />
                        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                            <ExpiringSubscriptions
                                organizations={dashboardData?.expiringSubscriptions}
                                isLoading={isLoading}
                            />
                            <RecentRegistrations
                                organizations={dashboardData?.recentRegistrations}
                                isLoading={isLoading}
                            />
                            <AttentionRequired
                                organizations={dashboardData?.attentionRequired}
                                isLoading={isLoading}
                            />
                        </div>
                    </div>
                </div>
            </main>
        </>
    );
}
