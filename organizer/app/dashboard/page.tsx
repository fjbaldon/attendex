"use client";

import * as React from "react";
import {AppSidebar} from "@/components/layout/app-sidebar";
import {ChartAreaInteractive} from "@/components/shared/chart-area-interactive";
import {SectionCards} from "@/components/layout/section-cards";
import {SiteHeader} from "@/components/layout/site-header";
import {SidebarInset, SidebarProvider,} from "@/components/ui/sidebar";
import {useDashboard} from "@/hooks/use-dashboard";
import {useIsMobile} from "@/hooks/use-mobile";

export default function DashboardPage() {
    const isMobile = useIsMobile();
    const [timeRange, setTimeRange] = React.useState("90d");
    const {stats, isLoadingStats, activity, isLoadingActivity} = useDashboard(timeRange);

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
                <div className="flex flex-1 flex-col">
                    <div className="@container/main flex flex-1 flex-col gap-2">
                        <div className="flex flex-col gap-4 py-4 md:gap-6 md:py-6">
                            <SectionCards stats={stats} isLoading={isLoadingStats}/>
                            <div className="px-4 lg:px-6">
                                <ChartAreaInteractive
                                    data={activity}
                                    isLoading={isLoadingActivity}
                                    timeRange={timeRange}
                                    setTimeRange={setTimeRange}
                                />
                            </div>
                        </div>
                    </div>
                </div>
            </SidebarInset>
        </SidebarProvider>
    );
}
