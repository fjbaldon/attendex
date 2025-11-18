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

type TabType = 'roster' | 'checked-in' | 'checked-out';

export default function EventDetailsPage() {
    const params = useParams();
    const eventId = Number(params.id);
    const [activeTab, setActiveTab] = React.useState<TabType>('checked-in');

    const [pagination, setPagination] = React.useState({
        pageIndex: 0,
        pageSize: 10,
    });

    const {
        event,
        isLoadingEvent,
        attendeesData,
        isLoadingAttendees,
        checkedInData,
        isLoadingCheckedIn,
        checkedOutData,
        isLoadingCheckedOut,
    } = useEventDetails(eventId, pagination);

    const {definitions: customFieldDefinitions, isLoading: isLoadingCustomFields} = useCustomFields();

    const handleTabChange = (tab: string) => {
        setActiveTab(tab as TabType);
        setPagination({pageIndex: 0, pageSize: 10});
    }

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
                <main className="flex flex-1 flex-col gap-4 p-4 lg:p-6">
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

                    <Tabs value={activeTab} onValueChange={handleTabChange}>
                        <TabsList>
                            <TabsTrigger value="roster">Roster</TabsTrigger>
                            <TabsTrigger value="checked-in">Checked-in</TabsTrigger>
                            <TabsTrigger value="checked-out">Checked-out</TabsTrigger>
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
                            />
                        </TabsContent>

                        <TabsContent value="checked-in">
                            <CheckedInAttendeesDataTable
                                columns={checkedInColumns}
                                data={checkedInData?.content ?? []}
                                pageCount={checkedInData?.totalPages ?? 0}
                                pagination={pagination}
                                setPagination={setPagination}
                                isLoading={isLoadingCheckedIn || isLoadingCustomFields}
                            />
                        </TabsContent>

                        <TabsContent value="checked-out">
                            <CheckedOutAttendeesDataTable
                                columns={checkedOutColumns}
                                data={checkedOutData?.content ?? []}
                                pageCount={checkedOutData?.totalPages ?? 0}
                                pagination={pagination}
                                setPagination={setPagination}
                                isLoading={isLoadingCheckedOut || isLoadingCustomFields}
                            />
                        </TabsContent>
                    </Tabs>
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}
