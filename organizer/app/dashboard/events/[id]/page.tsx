"use client";

import * as React from "react";
import {useParams} from 'next/navigation';
import {AppSidebar} from "@/components/layout/app-sidebar";
import {SiteHeader} from "@/components/layout/site-header";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {useEventDetails} from "@/hooks/use-event-details";
import {EventAttendeesDataTable} from "./event-attendees-data-table";
import {getColumns} from "./columns";
import {Skeleton} from "@/components/ui/skeleton";
import {
    Breadcrumb,
    BreadcrumbItem,
    BreadcrumbLink,
    BreadcrumbList,
    BreadcrumbPage,
    BreadcrumbSeparator
} from "@/components/ui/breadcrumb";

export default function EventDetailsPage() {
    const params = useParams();
    const eventId = Number(params.id);

    const {event, attendees, isLoadingEvent, isLoadingAttendees} = useEventDetails(eventId);

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
                            <p className="text-muted-foreground text-sm">
                                Manage the roster of attendees for this event.
                            </p>
                        </div>
                    </div>

                    <EventAttendeesDataTable
                        columns={columns}
                        data={attendees}
                        isLoading={isLoadingAttendees}
                        eventId={eventId}
                    />
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}
