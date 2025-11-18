"use client";

import * as React from "react";
import {AppSidebar} from "@/components/layout/app-sidebar";
import {ChartAreaInteractive} from "@/components/shared/chart-area-interactive";
import {SectionCards} from "@/components/layout/section-cards";
import {SiteHeader} from "@/components/layout/site-header";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {useDashboard} from "@/hooks/use-dashboard";
import {useIsMobile} from "@/hooks/use-mobile";
import {UpcomingEvents} from "@/components/layout/upcoming-events";
import {RecentEventsStats} from "@/components/layout/recent-events-stats";

export default function DashboardPage() {
    const isMobile = useIsMobile();
    const [timeRange, setTimeRange] = React.useState("90d");
    const {dashboardData, isLoading, activity} = useDashboard(timeRange);

    React.useEffect(() => {
        if (isMobile) {
            setTimeRange("7d");
        }
    }, [isMobile]);

    return (
        <SidebarProvider
            style={
                {
                    "--sidebar-width": "calc(var(--spacing) * 72)",
                    "--header-height": "calc(var(--spacing) * 12)",
                } as React.CSSProperties
            }
        >
            <AppSidebar variant="inset"/>
            <SidebarInset>
                <SiteHeader title="Dashboard"/>
                <main className="@container/main flex-1 overflow-y-auto p-4 lg:p-6">
                    <div className="flex flex-col gap-6">
                        {/* Section 1: KPIs */}
                        <SectionCards stats={dashboardData?.stats} isLoading={isLoading}/>

                        {/* Section 2: Main Charts & Lists */}
                        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                            <div className="lg:col-span-2">
                                <ChartAreaInteractive
                                    data={activity}
                                    isLoading={isLoading}
                                    timeRange={timeRange}
                                    setTimeRange={setTimeRange}
                                />
                            </div>
                            <div>
                                <UpcomingEvents events={dashboardData?.upcomingEvents} isLoading={isLoading}/>
                            </div>
                        </div>

                        {/* Section 3: Additional Stats */}
                        <div>
                            <RecentEventsStats events={dashboardData?.recentEventStats} isLoading={isLoading}/>
                        </div>
                    </div>
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}
