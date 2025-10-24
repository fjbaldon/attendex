"use client";

import * as React from "react";
import {useMemo, useState, useRef, useEffect} from "react";
import {AppSidebar} from "@/components/layout/app-sidebar";
import {SiteHeader} from "@/components/layout/site-header";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {useEvents} from "@/hooks/use-events";
import {useEventDetails} from "@/hooks/use-event-details";
import {Skeleton} from "@/components/ui/skeleton";
import {IconPrinter, IconFileSpreadsheet, IconLoader} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";
import {exportToPdf} from "@/lib/pdf-exporter";
import {ReportFilters} from "./report-filters";
import {AttendeeRoster} from "./attendee-roster";
import {useOrganization} from "@/hooks/use-organization";
import {useCustomFields} from "@/hooks/use-custom-fields";

export default function ReportsPage() {
    const [selectedEventId, setSelectedEventId] = useState<number | null>(null);
    const [activeFilters, setActiveFilters] = useState<Record<string, string[]>>({});
    const [isExporting, setIsExporting] = useState(false);

    const rosterRef = useRef<HTMLDivElement>(null);

    const {events, isLoadingEvents} = useEvents();
    const {attendees: allAttendees, isLoadingAttendees} = useEventDetails(selectedEventId);
    const {organization} = useOrganization();

    const {definitions: customFieldDefinitions, isLoading: isLoadingCustomFields} = useCustomFields();

    const customFields = useMemo(() =>
            customFieldDefinitions.map(def => def.fieldName),
        [customFieldDefinitions]
    );

    // --- ADD THIS ENTIRE useEffect BLOCK FOR DEBUGGING ---
    useEffect(() => {
        // This will run whenever the attendee list changes after a selection
        if (allAttendees && allAttendees.length > 0) {
            console.log("--- DEBUG: Attendee Data Received ---");
            console.log("Total attendees loaded:", allAttendees.length);
            console.log("Data for the first attendee:", allAttendees[0]);
            console.log("Custom fields for the first attendee:", allAttendees[0]?.customFields);
        }
    }, [allAttendees]);

    const selectedEvent = events.find(e => e.id === selectedEventId);

    const filteredAttendees = useMemo(() => {
        const filterKeys = Object.keys(activeFilters).filter(key => activeFilters[key].length > 0);
        if (filterKeys.length === 0) {
            return allAttendees;
        }

        return allAttendees.filter(attendee => {
            return filterKeys.every(field => {
                const selectedValues = activeFilters[field];
                const attendeeValue = attendee.customFields?.[field];
                return selectedValues.includes(String(attendeeValue));
            });
        });
    }, [allAttendees, activeFilters]);

    const handleExport = async () => {
        if (!rosterRef.current || !selectedEvent) return;

        setIsExporting(true);
        const fileName = `roster-${selectedEvent.eventName.replace(/ /g, '_')}`;
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
                        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                            <div>
                                <h1 className="text-lg font-semibold md:text-2xl">Attendee Roster Generation</h1>
                                <p className="text-muted-foreground text-sm">Select an event, apply filters, and print a
                                    PDF roster.</p>
                            </div>
                            <div className="flex items-center gap-2">
                                {isLoadingEvents ? <Skeleton className="h-9 w-64"/> : (
                                    <Select value={selectedEventId ? String(selectedEventId) : ""}
                                            onValueChange={handleEventChange}>
                                        <SelectTrigger className="w-full sm:w-64"><SelectValue
                                            placeholder="Select an Event"/></SelectTrigger>
                                        <SelectContent>
                                            {events.map(event => (<SelectItem key={event.id}
                                                                              value={String(event.id)}>{event.eventName}</SelectItem>))}
                                        </SelectContent>
                                    </Select>
                                )}
                                <Button onClick={handleExport} disabled={!selectedEventId || isExporting}>
                                    <IconPrinter className="mr-2 h-4 w-4" stroke={1.5}/>
                                    {isExporting ? 'Exporting...' : 'Print PDF'}
                                </Button>
                            </div>
                        </div>

                        {selectedEventId && (
                            isLoadingCustomFields || isLoadingAttendees ? (
                                <Skeleton className="h-12 w-full"/>
                            ) : (
                                <ReportFilters
                                    attendees={allAttendees}
                                    customFields={customFields}
                                    activeFilters={activeFilters}
                                    onFilterChange={setActiveFilters}
                                />
                            )
                        )}

                        <div className="pt-4">
                            {selectedEventId ? (
                                isLoadingAttendees ? (
                                    <div className="flex items-center justify-center h-96"><IconLoader
                                        className="h-8 w-8 animate-spin" stroke={1.5}/></div>
                                ) : (
                                    <AttendeeRoster
                                        ref={rosterRef}
                                        eventName={selectedEvent?.eventName ?? '...'}
                                        organizationName={organization?.name ?? ''}
                                        totalAttendees={allAttendees.length}
                                        filteredAttendees={filteredAttendees}
                                        customFields={customFields}
                                        activeFilters={activeFilters}
                                    />
                                )
                            ) : (
                                <div
                                    className="flex flex-col h-96 items-center justify-center rounded-lg border-2 border-dashed text-center">
                                    <IconFileSpreadsheet className="h-16 w-16 text-muted-foreground mb-4" stroke={1.5}/>
                                    <h2 className="text-xl font-semibold">Select an Event to Begin</h2>
                                    <p className="text-muted-foreground mt-2">Choose an event from the dropdown above to
                                        view and filter its attendee roster.</p>
                                </div>
                            )}
                        </div>
                    </div>
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}
