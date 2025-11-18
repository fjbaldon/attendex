"use client";

import * as React from "react";
import {AppSidebar} from "@/components/layout/app-sidebar";
import {SiteHeader} from "@/components/layout/site-header";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {useEvents} from "@/hooks/use-events";
import {EventsDataTable} from "./events-data-table";
import {getColumns} from "./columns";
import {useRouter} from "next/navigation";

export default function EventsPage() {
    const [{pageIndex, pageSize}, setPagination] = React.useState({
        pageIndex: 0,
        pageSize: 10,
    });

    const router = useRouter(); // <-- Router is created here

    const {eventsData, isLoadingEvents} = useEvents(pageIndex, pageSize);
    const events = eventsData?.content ?? [];
    const pageCount = eventsData?.totalPages ?? 0;

    // useMemo ensures that getColumns is only re-run if the router instance changes (which it won't).
    // This correctly "bakes in" the router instance for the column definitions.
    const columns = React.useMemo(() => getColumns(router), [router]);

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
                <SiteHeader title="Events"/>
                <div className="flex flex-1 flex-col">
                    <div className="@container/main flex flex-1 flex-col gap-4 py-4 md:gap-6 md:py-6">
                        <div className="px-4 lg:px-6">
                            <EventsDataTable
                                columns={columns} // <-- The correct columns are passed here
                                data={events}
                                isLoading={isLoadingEvents}
                                pageCount={pageCount}
                                pagination={{pageIndex, pageSize}}
                                setPagination={setPagination}
                            />
                        </div>
                    </div>
                </div>
            </SidebarInset>
        </SidebarProvider>
    );
}
