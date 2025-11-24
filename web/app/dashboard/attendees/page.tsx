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
import {useDebounce} from "@uidotdev/usehooks";

export default function AttendeesPage() {
    const [{pageIndex, pageSize}, setPagination] = React.useState({
        pageIndex: 0,
        pageSize: 10,
    });

    // 1. LIFTED STATE: Manage search and filters here
    const [searchQuery, setSearchQuery] = React.useState("");
    const [activeFilters, setActiveFilters] = React.useState<Record<string, string>>({});

    // Debounce the search query to prevent excessive API calls
    const debouncedQuery = useDebounce(searchQuery, 500);

    // 2. PASS STATE TO HOOK: Now fetching includes the filters
    const {attendeesData, isLoadingAttendees} = useAttendees(
        pageIndex,
        pageSize,
        debouncedQuery,
        activeFilters
    );

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
                        <div className="w-full max-w-[1600px] mx-auto px-4 lg:px-6">
                            <AttendeesDataTable
                                columns={columns}
                                data={attendees}
                                isLoading={isLoadingAttendees || isLoadingAttributes}
                                pageCount={pageCount}
                                pagination={{pageIndex, pageSize}}
                                setPagination={setPagination}
                                // 3. PASS PROPS DOWN
                                searchQuery={searchQuery}
                                onSearchChange={setSearchQuery}
                                activeFilters={activeFilters}
                                onFiltersChange={setActiveFilters}
                            />
                        </div>
                    </div>
                </div>
            </SidebarInset>
        </SidebarProvider>
    );
}
