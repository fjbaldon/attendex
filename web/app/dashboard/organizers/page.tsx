"use client";

import * as React from "react";
import {AppSidebar} from "@/components/layout/app-sidebar";
import {SiteHeader} from "@/components/layout/site-header";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {useOrganizers} from "@/hooks/use-organizers";
import {columns} from "./columns";
import {OrganizersDataTable} from "./organizers-data-table";
import {useDebounce} from "@uidotdev/usehooks";

export default function OrganizersPage() {
    const [{pageIndex, pageSize}, setPagination] = React.useState({
        pageIndex: 0,
        pageSize: 10,
    });

    // FIX: Server-side search state
    const [searchQuery, setSearchQuery] = React.useState("");
    const debouncedQuery = useDebounce(searchQuery, 500);

    const {organizersData, isLoadingOrganizers} = useOrganizers(pageIndex, pageSize, debouncedQuery);
    const organizers = organizersData?.content ?? [];
    const pageCount = organizersData?.totalPages ?? 0;

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
                <SiteHeader title="Organizers"/>
                <div className="flex flex-1 flex-col">
                    <div className="@container/main flex flex-1 flex-col gap-4 py-4 md:gap-6 md:py-6">
                        <div className="w-full max-w-6xl mx-auto px-4 lg:px-6">
                            <OrganizersDataTable
                                columns={columns}
                                data={organizers}
                                isLoading={isLoadingOrganizers}
                                pageCount={pageCount}
                                pagination={{pageIndex, pageSize}}
                                setPagination={setPagination}
                                // Pass search control
                                onSearchChange={setSearchQuery}
                                searchValue={searchQuery}
                            />
                        </div>
                    </div>
                </div>
            </SidebarInset>
        </SidebarProvider>
    );
}
