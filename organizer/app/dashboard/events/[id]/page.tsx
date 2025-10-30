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
import {getCheckedInColumns} from "./checked-in-columns";
import {CheckedInAttendeesDataTable} from "./checked-in-attendees-data-table";
import {useCustomFields} from "@/hooks/use-custom-fields";
import {getCheckedOutColumns} from "./checked-out-columns";
import {CheckedOutAttendeesDataTable} from "./checked-out-attendees-data-table";

export default function EventDetailsPage() {
    const params = useParams();
    const eventId = Number(params.id);

    const {
        event,
        attendees,
        checkedInAttendees,
        checkedOutAttendees,
        isLoadingEvent,
        isLoadingAttendees,
        isLoadingCheckedIn,
        isLoadingCheckedOut
    } = useEventDetails(eventId);

    const {definitions: customFieldDefinitions, isLoading: isLoadingCustomFields} = useCustomFields();

    const rosterColumns = React.useMemo(() => getRosterColumns(customFieldDefinitions), [customFieldDefinitions]);

    const checkedInColumns = React.useMemo(() => getCheckedInColumns(customFieldDefinitions), [customFieldDefinitions]);

    const checkedOutColumns = React.useMemo(() => getCheckedOutColumns(customFieldDefinitions), [customFieldDefinitions]);

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
                <main className="flex flex-1 flex-col gap-4 p-4 lg:gap-6 lg:p-6">
                    <Breadcrumb>
                        <BreadcrumbList>
                            <BreadcrumbItem>
                                <BreadcrumbLink href="/dashboard/events">Events</BreadcrumbLink>
                            </BreadcrumbItem>
                            <BreadcrumbSeparator/>
                            <BreadcrumbItem>
                                {isLoadingEvent ? <Skeleton className="h-5 w-32"/> :
                                    <BreadcrumbPage>{event?.eventName}</BreadcrumbPage>}
                            </BreadcrumbItem>
                        </BreadcrumbList>
                    </Breadcrumb>

                    <div className="flex items-center">
                        <div>
                            {isLoadingEvent ? <Skeleton className="h-8 w-64"/> :
                                <h1 className="text-lg font-semibold md:text-2xl">{event?.eventName}</h1>}
                        </div>
                    </div>

                    <Tabs defaultValue="checked-in">
                        <TabsList>
                            <TabsTrigger value="roster">Roster</TabsTrigger>
                            <TabsTrigger value="checked-in">Checked-in</TabsTrigger>
                            <TabsTrigger value="checked-out">Checked-out</TabsTrigger>
                        </TabsList>

                        <TabsContent value="roster">
                            <EventAttendeesDataTable
                                columns={rosterColumns}
                                data={attendees}
                                isLoading={isLoadingAttendees || isLoadingCustomFields}
                                eventId={eventId}
                            />
                        </TabsContent>

                        <TabsContent value="checked-in">
                            <CheckedInAttendeesDataTable
                                columns={checkedInColumns}
                                data={checkedInAttendees}
                                isLoading={isLoadingCheckedIn || isLoadingCustomFields}
                            />
                        </TabsContent>

                        <TabsContent value="checked-out">
                            <CheckedOutAttendeesDataTable
                                columns={checkedOutColumns}
                                data={checkedOutAttendees}
                                isLoading={isLoadingCheckedOut || isLoadingCustomFields}
                            />
                        </TabsContent>
                    </Tabs>
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}
