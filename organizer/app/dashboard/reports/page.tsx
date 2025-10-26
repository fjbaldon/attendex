"use client";

import * as React from "react";
import {useMemo, useState, useRef} from "react";
import {AppSidebar} from "@/components/layout/app-sidebar";
import {SiteHeader} from "@/components/layout/site-header";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {useEventDetails} from "@/hooks/use-event-details";
import {Skeleton} from "@/components/ui/skeleton";
import {IconPrinter, IconReportAnalytics, IconLoader} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";
import {exportToPdf} from "@/lib/pdf-exporter";
import {ReportFilters} from "./report-filters";
import {AttendeeRoster} from "./attendee-roster";
import {useOrganization} from "@/hooks/use-organization";
import {useCustomFields} from "@/hooks/use-custom-fields";
import {useEvents} from "@/hooks/use-events";

export default function ReportsPage() {
    const [selectedEventId, setSelectedEventId] = useState<number | null>(null);
    const [activeFilters, setActiveFilters] = useState<Record<string, string[]>>({});
    const [isExporting, setIsExporting] = useState(false);

    const rosterRef = useRef<HTMLDivElement>(null);

    const {events, isLoadingEvents} = useEvents();
    const {checkedInAttendees, isLoadingCheckedIn} = useEventDetails(selectedEventId);
    const {organization} = useOrganization();
    const {definitions: customFieldDefinitions, isLoading: isLoadingCustomFields} = useCustomFields();

    const customFields = useMemo(() =>
        customFieldDefinitions.map(def => def.fieldName), [customFieldDefinitions]
    );

    const selectedEvent = events.find(e => e.id === selectedEventId);

    const filteredAttendees = useMemo(() => {
        const sourceAttendees = checkedInAttendees;
        const filterKeys = Object.keys(activeFilters).filter(key => activeFilters[key].length > 0);
        if (filterKeys.length === 0) {
            return sourceAttendees;
        }

        return sourceAttendees.filter(attendee => {
            return filterKeys.every(field => {
                const selectedValues = activeFilters[field];
                const attendeeValue = attendee.customFields?.[field];
                return selectedValues.includes(String(attendeeValue));
            });
        });
    }, [checkedInAttendees, activeFilters]);

    const handleExport = async () => {
        if (!rosterRef.current || !selectedEvent) return;

        setIsExporting(true);
        const fileName = `checked-in-${selectedEvent.eventName.replace(/ /g, '_')}`;
        try {
            await exportToPdf(rosterRef.current, fileName);
        } catch (error) {
            console.error("Failed to export PDF:", error);
        } finally {
            setIsExporting(false);
        }
    };

    const handleEventChange = (value: string) => {
        setSelectedEventId(Number(value));
        setActiveFilters({});
    };

    const isLoading = isLoadingCheckedIn || isLoadingCustomFields;

    return (
        <SidebarProvider style={{
            "--sidebar-width": "calc(var(--spacing) * 72)",
            "--header-height": "calc(var(--spacing) * 12)"
        } as React.CSSProperties}>
            <AppSidebar variant="inset"/>
            <SidebarInset>
                <SiteHeader title="Reports"/>
                <main className="flex-1 p-4 lg:p-6">
                    <div className="w-full max-w-6xl mx-auto space-y-6">
                        {/* Header Section */}
                        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-start">
                            <div className="flex flex-shrink-0 items-center gap-2">
                                {isLoadingEvents ? <Skeleton className="h-9 w-full sm:w-64"/> : (
                                    <Select value={selectedEventId ? String(selectedEventId) : ""}
                                            onValueChange={handleEventChange}>
                                        <SelectTrigger className="w-full sm:w-64">
                                            <SelectValue placeholder="Select an Event"/>
                                        </SelectTrigger>
                                        <SelectContent>
                                            {events.map(event => (
                                                <SelectItem key={event.id}
                                                            value={String(event.id)}>{event.eventName}</SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>
                                )}
                                <Button onClick={handleExport} disabled={!selectedEventId || isExporting}
                                        variant="outline">
                                    <IconPrinter className="mr-2 h-4 w-4" stroke={1.5}/>
                                    {isExporting ? 'Exporting...' : 'Export PDF'}
                                </Button>
                            </div>
                        </div>

                        {/* Filter & Roster Section */}
                        {selectedEventId && (
                            <div className="space-y-6">
                                <div className="rounded-lg border bg-card p-4">
                                    {isLoading ? (
                                        <Skeleton className="h-8 w-full"/>
                                    ) : (
                                        <ReportFilters
                                            attendees={checkedInAttendees}
                                            customFields={customFields}
                                            activeFilters={activeFilters}
                                            onFilterChange={setActiveFilters}
                                        />
                                    )}
                                </div>
                                <div className="pt-2">
                                    {isLoading ? (
                                        <div className="flex items-center justify-center h-96">
                                            <IconLoader className="h-8 w-8 animate-spin" stroke={1.5}/>
                                        </div>
                                    ) : (
                                        <AttendeeRoster
                                            ref={rosterRef}
                                            listTitle="Checked-in Attendees"
                                            eventName={selectedEvent?.eventName ?? '...'}
                                            organizationName={organization?.name ?? ''}
                                            totalAttendees={checkedInAttendees.length}
                                            filteredAttendees={filteredAttendees}
                                            customFields={customFields}
                                            activeFilters={activeFilters}
                                        />
                                    )}
                                </div>
                            </div>
                        )}

                        {/* Empty State */}
                        {!selectedEventId && (
                            <div
                                className="flex flex-col h-96 items-center justify-center rounded-lg border-2 border-dashed text-center">
                                <IconReportAnalytics className="h-16 w-16 text-muted-foreground mb-4" stroke={1.5}/>
                                <h2 className="text-xl font-semibold">Select an Event to Begin</h2>
                                <p className="text-muted-foreground mt-2">
                                    Choose an event to view and filter its checked-in attendees.
                                </p>
                            </div>
                        )}
                    </div>
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}
