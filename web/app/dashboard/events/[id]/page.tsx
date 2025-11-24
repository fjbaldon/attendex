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
import {getArrivalsColumns} from "./arrivals-columns";
import {ArrivalsDataTable} from "./arrivals-data-table";
import {useAttributes} from "@/hooks/use-attributes";
import {getDeparturesColumns} from "./departures-columns";
import {DeparturesDataTable} from "./departures-data-table";
import {useDebounce} from "@uidotdev/usehooks";

type TabType = 'roster' | 'arrival' | 'departure';

export default function EventDetailsPage() {
    const params = useParams();
    const eventId = Number(params.id);
    const [activeTab, setActiveTab] = React.useState<TabType>('arrival');

    const [pagination, setPagination] = React.useState({
        pageIndex: 0,
        pageSize: 10,
    });

    // FIX: Independent search state per tab to preserve user input
    const [searchState, setSearchState] = React.useState({
        roster: "",
        arrival: "",
        departure: ""
    });

    const debouncedRoster = useDebounce(searchState.roster, 500);
    const debouncedArrival = useDebounce(searchState.arrival, 500);
    const debouncedDeparture = useDebounce(searchState.departure, 500);

    const searchParams = React.useMemo(() => ({
        rosterQuery: debouncedRoster,
        arrivalsQuery: debouncedArrival,
        departuresQuery: debouncedDeparture,
    }), [debouncedRoster, debouncedArrival, debouncedDeparture]);

    const {
        event,
        isLoadingEvent,
        attendeesData,
        isLoadingAttendees,
        arrivalsData,
        isLoadingArrivals,
        departuresData,
        isLoadingDepartures,
    } = useEventDetails(eventId, pagination, searchParams);

    const {definitions: customFieldDefinitions, isLoading: isLoadingCustomFields} = useAttributes();

    const handleTabChange = (tab: string) => {
        setActiveTab(tab as TabType);
        setPagination({pageIndex: 0, pageSize: 10});
        // Note: We do NOT reset search state here anymore, improving UX.
    }

    const updateSearch = (val: string) => {
        setSearchState(prev => ({
            ...prev,
            [activeTab]: val
        }));
        setPagination(prev => ({ ...prev, pageIndex: 0 }));
    };

    const rosterColumns = React.useMemo(() => getRosterColumns(customFieldDefinitions), [customFieldDefinitions]);
    const checkedInColumns = React.useMemo(() => getArrivalsColumns(customFieldDefinitions), [customFieldDefinitions]);
    const checkedOutColumns = React.useMemo(() => getDeparturesColumns(customFieldDefinitions), [customFieldDefinitions]);

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

                        <div className="flex items-center">
                            <div>
                                {isLoadingEvent ? <Skeleton className="h-8 w-64"/> :
                                    <h1 className="text-lg font-semibold md:text-2xl">{event?.name}</h1>}
                            </div>
                        </div>

                        <Tabs value={activeTab} onValueChange={handleTabChange}>
                            <TabsList>
                                <TabsTrigger value="roster">Roster</TabsTrigger>
                                <TabsTrigger value="arrival">Arrival</TabsTrigger>
                                <TabsTrigger value="departure">Departure</TabsTrigger>
                            </TabsList>

                            <TabsContent value="roster">
                                <EventAttendeesDataTable
                                    columns={rosterColumns}
                                    data={attendeesData?.content ?? []}
                                    pageCount={attendeesData?.totalPages ?? 0}
                                    pagination={pagination}
                                    setPagination={setPagination}
                                    isLoading={isLoadingAttendees || isLoadingCustomFields}
                                    eventId={eventId}
                                    searchQuery={searchState.roster}
                                    onSearchChange={updateSearch}
                                />
                            </TabsContent>

                            <TabsContent value="arrival">
                                <ArrivalsDataTable
                                    columns={checkedInColumns}
                                    data={arrivalsData?.content ?? []}
                                    pageCount={arrivalsData?.totalPages ?? 0}
                                    pagination={pagination}
                                    setPagination={setPagination}
                                    isLoading={isLoadingArrivals || isLoadingCustomFields}
                                    searchQuery={searchState.arrival}
                                    onSearchChange={updateSearch}
                                />
                            </TabsContent>

                            <TabsContent value="departure">
                                <DeparturesDataTable
                                    columns={checkedOutColumns}
                                    data={departuresData?.content ?? []}
                                    pageCount={departuresData?.totalPages ?? 0}
                                    pagination={pagination}
                                    setPagination={setPagination}
                                    isLoading={isLoadingDepartures || isLoadingCustomFields}
                                    searchQuery={searchState.departure}
                                    onSearchChange={updateSearch}
                                />
                            </TabsContent>
                        </Tabs>
                    </div>
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}
