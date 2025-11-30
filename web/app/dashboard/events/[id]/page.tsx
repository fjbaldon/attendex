"use client";

import * as React from "react";
import {useParams} from 'next/navigation';
import {AppSidebar} from "@/components/layout/app-sidebar";
import {SiteHeader} from "@/components/layout/site-header";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {useEventDetails} from "@/hooks/use-event-details";
import {EventAttendeesDataTable} from "./event-attendees-data-table";
import {getColumns as getRosterColumns} from "./columns";
import {Skeleton} from "@/components/ui/skeleton";
import {
    Breadcrumb,
    BreadcrumbItem,
    BreadcrumbLink,
    BreadcrumbList,
    BreadcrumbPage,
    BreadcrumbSeparator
} from "@/components/ui/breadcrumb";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {useAttributes} from "@/hooks/use-attributes";
import {useDebounce} from "@uidotdev/usehooks";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Label} from "@/components/ui/label";
import {getActivityColumns} from "./activity-columns";
import {ActivityDataTable} from "./activity-data-table";

type TabType = 'roster' | 'activity';

export default function EventDetailsPage() {
    const params = useParams();
    const eventId = Number(params.id);
    const [activeTab, setActiveTab] = React.useState<TabType>('activity');

    const [pagination, setPagination] = React.useState({
        pageIndex: 0,
        pageSize: 10,
    });

    const [searchState, setSearchState] = React.useState({
        roster: "",
        activity: "",
    });

    // Attribute Filters
    const [activeFilters, setActiveFilters] = React.useState<Record<string, string>>({});

    const [selectedSessionId, setSelectedSessionId] = React.useState<string>("ALL");

    const debouncedRoster = useDebounce(searchState.roster, 500);
    const debouncedActivity = useDebounce(searchState.activity, 500);

    // FIX: Only pass filters for the CURRENT tab to avoid confusing queries?
    // Actually, usually filters persist per tab or global?
    // Let's keep filters distinct if possible, but for simplicity here, we'll use one state object
    // and reset it when tab changes, or just share it. The logic below shares it.
    // Ideally, reset filtering when switching tabs.
    const handleTabChange = (tab: string) => {
        setActiveTab(tab as TabType);
        setPagination({pageIndex: 0, pageSize: 10});
        setActiveFilters({}); // Reset attribute filters
        setSelectedSessionId("ALL"); // Reset session filter
    }

    const searchParams = React.useMemo(() => ({
        rosterQuery: debouncedRoster,
        activityQuery: debouncedActivity,
        sessionId: selectedSessionId !== "ALL" ? Number(selectedSessionId) : null
    }), [debouncedRoster, debouncedActivity, selectedSessionId]);

    const {
        event,
        isLoadingEvent,
        attendeesData,
        isLoadingAttendees,
        activityData,
        isLoadingActivity,
    } = useEventDetails(eventId, pagination, searchParams, activeFilters);

    const {definitions: attributes, isLoading: isLoadingAttributes} = useAttributes();

    const updateSearch = (val: string) => {
        setSearchState(prev => ({
            ...prev,
            [activeTab]: val
        }));
        setPagination(prev => ({ ...prev, pageIndex: 0 }));
    };

    const rosterColumns = React.useMemo(() => getRosterColumns(attributes), [attributes]);
    const activityColumns = React.useMemo(() => getActivityColumns(attributes, event?.sessions), [attributes, event?.sessions]);

    return (
        <SidebarProvider
            style={{
                "--sidebar-width": "calc(var(--spacing) * 72)",
                "--header-height": "calc(var(--spacing) * 12)",
            } as React.CSSProperties}
        >
            <AppSidebar variant="inset"/>
            <SidebarInset>
                <SiteHeader title="Events"/>
                <main className="flex flex-1 flex-col p-4 lg:p-6">
                    <div className="w-full max-w-[1600px] mx-auto flex flex-col gap-4">
                        <Breadcrumb>
                            <BreadcrumbList>
                                <BreadcrumbItem>
                                    <BreadcrumbLink href="/dashboard/events">Events</BreadcrumbLink>
                                </BreadcrumbItem>
                                <BreadcrumbSeparator/>
                                <BreadcrumbItem>
                                    {isLoadingEvent ? <Skeleton className="h-5 w-32"/> :
                                        <BreadcrumbPage>{event?.name}</BreadcrumbPage>}
                                </BreadcrumbItem>
                            </BreadcrumbList>
                        </Breadcrumb>

                        <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
                            <div>
                                {isLoadingEvent ? <Skeleton className="h-8 w-64"/> :
                                    <h1 className="text-lg font-semibold md:text-2xl">{event?.name}</h1>}
                            </div>
                        </div>

                        <Tabs value={activeTab} onValueChange={handleTabChange}>
                            <TabsList>
                                <TabsTrigger value="activity">Activity Log</TabsTrigger>
                                <TabsTrigger value="roster">Roster</TabsTrigger>
                            </TabsList>

                            <TabsContent value="activity">
                                <ActivityDataTable
                                    columns={activityColumns}
                                    data={activityData?.content ?? []}
                                    pageCount={activityData?.totalPages ?? 0}
                                    pagination={pagination}
                                    setPagination={setPagination}
                                    isLoading={isLoadingActivity || isLoadingAttributes}
                                    searchQuery={searchState.activity}
                                    onSearchChange={updateSearch}
                                    activeFilters={activeFilters}
                                    onFiltersChange={setActiveFilters}
                                    attributes={attributes}
                                    toolbarChildren={
                                        // Inject Session Filter into the Toolbar
                                        <div className="flex items-center gap-2 border-l pl-4 ml-2">
                                            <Label className="text-xs text-muted-foreground whitespace-nowrap">Session:</Label>
                                            <Select value={selectedSessionId} onValueChange={(val) => {
                                                setSelectedSessionId(val);
                                                setPagination(prev => ({...prev, pageIndex: 0}));
                                            }}>
                                                <SelectTrigger className="w-[200px] h-9 text-xs">
                                                    <SelectValue placeholder="All Sessions"/>
                                                </SelectTrigger>
                                                <SelectContent>
                                                    <SelectItem value="ALL">All Activity</SelectItem>
                                                    {event?.sessions?.map(session => (
                                                        <SelectItem key={session.id} value={String(session.id)}>
                                                            {session.activityName}
                                                        </SelectItem>
                                                    ))}
                                                </SelectContent>
                                            </Select>
                                        </div>
                                    }
                                />
                            </TabsContent>

                            <TabsContent value="roster">
                                <EventAttendeesDataTable
                                    columns={rosterColumns}
                                    data={attendeesData?.content ?? []}
                                    pageCount={attendeesData?.totalPages ?? 0}
                                    pagination={pagination}
                                    setPagination={setPagination}
                                    isLoading={isLoadingAttendees || isLoadingAttributes}
                                    eventId={eventId}
                                    searchQuery={searchState.roster}
                                    onSearchChange={updateSearch}
                                    // Pass new filter props (needs update in component)
                                    activeFilters={activeFilters}
                                    onFiltersChange={setActiveFilters}
                                    attributes={attributes}
                                />
                            </TabsContent>
                        </Tabs>
                    </div>
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}
