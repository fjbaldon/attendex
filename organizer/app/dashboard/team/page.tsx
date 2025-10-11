"use client";

import * as React from "react";
import {AppSidebar} from "@/components/layout/app-sidebar";
import {SiteHeader} from "@/components/layout/site-header";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {useUsers} from "@/hooks/use-users";
import {columns} from "./columns";
import {TeamDataTable} from "./team-data-table";

export default function TeamPage() {
    const [{pageIndex, pageSize}, setPagination] = React.useState({
        pageIndex: 0,
        pageSize: 10,
    });

    const {organizers, pageInfo, isLoadingOrganizers} = useUsers(pageIndex, pageSize);
    const pageCount = pageInfo?.totalPages ?? 0;

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
                <SiteHeader title="Team"/>
                <div className="flex flex-1 flex-col">
                    <div className="@container/main flex flex-1 flex-col gap-4 py-4 md:gap-6 md:py-6">
                        <div className="px-4 lg:px-6">
                            <TeamDataTable
                                columns={columns}
                                data={organizers}
                                isLoading={isLoadingOrganizers}
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
