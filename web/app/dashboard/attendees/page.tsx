"use client";

import * as React from "react";
import {useMemo} from "react";
import {AppSidebar} from "@/components/layout/app-sidebar";
import {SiteHeader} from "@/components/layout/site-header";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {useAttendees} from "@/hooks/use-attendees";
import {getColumns} from "./columns";
import {AttendeesDataTable} from "./attendees-data-table";
import {useAttributes} from "@/hooks/use-attributes";

export default function AttendeesPage() {
    const [{pageIndex, pageSize}, setPagination] = React.useState({
        pageIndex: 0,
        pageSize: 10,
    });

    const {attendeesData, isLoadingAttendees} = useAttendees(pageIndex, pageSize);
    const {definitions: attributes, isLoading: isLoadingAttributes} = useAttributes();

    const attendees = attendeesData?.content ?? [];
    const pageCount = attendeesData?.totalPages ?? 0;

    const columns = useMemo(() => getColumns(attributes), [attributes]);

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
                <SiteHeader title="Attendees"/>
                <div className="flex flex-1 flex-col">
                    <div className="@container/main flex flex-1 flex-col gap-4 py-4 md:gap-6 md:py-6">
                        <div className="px-4 lg:px-6">
                            <AttendeesDataTable
                                columns={columns}
                                data={attendees}
                                isLoading={isLoadingAttendees || isLoadingAttributes}
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
