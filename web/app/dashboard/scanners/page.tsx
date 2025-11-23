"use client";

import * as React from "react";
import {AppSidebar} from "@/components/layout/app-sidebar";
import {SiteHeader} from "@/components/layout/site-header";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {useScanners} from "@/hooks/use-scanners";
import {getColumns} from "./columns";
import {ScannersDataTable} from "./scanners-data-table";

export default function ScannersPage() {
    const [{pageIndex, pageSize}, setPagination] = React.useState({
        pageIndex: 0,
        pageSize: 10,
    });

    const {scannersData, isLoadingScanners} = useScanners(pageIndex, pageSize);
    const scanners = scannersData?.content ?? [];
    const pageCount = scannersData?.totalPages ?? 0;

    const columns = React.useMemo(() => getColumns(), []);

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
                <SiteHeader title="Scanners"/>
                <div className="flex flex-1 flex-col">
                    <div className="@container/main flex flex-1 flex-col gap-4 py-4 md:gap-6 md:py-6">
                        <div className="w-full max-w-6xl mx-auto px-4 lg:px-6">
                            <ScannersDataTable
                                columns={columns}
                                data={scanners}
                                isLoading={isLoadingScanners}
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
