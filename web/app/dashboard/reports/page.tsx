"use client";

import * as React from "react";
import {useRef, useState} from "react";
import {AppSidebar} from "@/components/layout/app-sidebar";
import {SiteHeader} from "@/components/layout/site-header";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {useEventDetails} from "@/hooks/use-event-details";
import {Skeleton} from "@/components/ui/skeleton";
import {IconLoader, IconPrinter, IconReportAnalytics} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";
import {useEvents} from "@/hooks/use-events";
import {Card} from "@/components/ui/card";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {format} from "date-fns";
import {useOrganization} from "@/hooks/use-organization";
import {Separator} from "@/components/ui/separator";
import {toast} from "sonner";
import {EntryDetailsDto} from "@/types";

export default function ReportsPage() {
    const [selectedEventId, setSelectedEventId] = useState<number | null>(null);
    const [isExporting, setIsExporting] = useState(false);
    const reportContentRef = useRef<HTMLDivElement>(null);

    const [pagination] = React.useState({pageIndex: 0, pageSize: 1000});

    const {eventsData, isLoadingEvents} = useEvents(0, 9999);
    const events = eventsData?.content ?? [];

    const {arrivalsData, isLoadingArrivals} = useEventDetails(selectedEventId, pagination);
    const {organization} = useOrganization();

    const entries = arrivalsData?.content ?? [];
    const totalElements = arrivalsData?.totalElements ?? 0; // Get total count from API

    const selectedEvent = events.find(e => e.id === selectedEventId);
    const generationDate = new Date().toLocaleString();

    const handleExport = async () => {
        if (!reportContentRef.current || !selectedEvent) {
            toast.error("Please select an event to export.");
            return;
        }

        setIsExporting(true);
        toast.info("Preparing PDF...", {description: "This may take a moment for large reports."});

        try {
            const {exportToPdf} = await import("@/lib/pdf-exporter");
            const fileName = `report-${selectedEvent.name.replace(/ /g, '_')}-arrivals`;
            await exportToPdf(reportContentRef.current, fileName);
        } catch (error) {
            console.error("Failed to export PDF:", error);
            toast.error("Export Failed", {description: "An unexpected error occurred while generating the PDF."});
        } finally {
            setIsExporting(false);
        }
    };

    const handleEventChange = (value: string) => {
        setSelectedEventId(Number(value));
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
                                                <SelectItem key={event.id} value={String(event.id)}>
                                                    {event.name}
                                                </SelectItem>
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

                        <div className="pt-2">
                            {selectedEventId ? (
                                <>
                                    {isLoadingArrivals ? (
                                        <div className="flex items-center justify-center h-96">
                                            <IconLoader className="h-8 w-8 animate-spin" stroke={1.5}/>
                                        </div>
                                    ) : (
                                        <Card ref={reportContentRef} className="p-8 bg-white text-black">
                                            <header className="mb-6">
                                                <h2 className="text-3xl font-bold">{selectedEvent?.name}</h2>
                                                <p className="text-lg text-gray-500">Arrivals Report</p>

                                                {totalElements > 1000 && (
                                                    <p className="text-sm text-red-500 mt-2">
                                                        Note: This report is limited to the first 1000 entries for
                                                        performance.
                                                        Please contact support for a full data export.
                                                    </p>
                                                )}
                                            </header>

                                            <section className="grid grid-cols-3 gap-4 mb-6 text-sm">
                                                <div className="space-y-1">
                                                    <p className="font-semibold">Total Arrivals:</p>
                                                    <p className="text-gray-600">{entries.length}</p>
                                                </div>
                                            </section>

                                            <Separator className="mb-6 bg-gray-200"/>

                                            <Table>
                                                <TableHeader>
                                                    <TableRow className="border-b-gray-300">
                                                        <TableHead className="text-black">Identity</TableHead>
                                                        <TableHead className="text-black">Last Name</TableHead>
                                                        <TableHead className="text-black">First Name</TableHead>
                                                        <TableHead className="text-right text-black">
                                                            Arrival Time
                                                        </TableHead>
                                                    </TableRow>
                                                </TableHeader>
                                                <TableBody>
                                                    {entries.length > 0 ? (
                                                        entries.map((entry: EntryDetailsDto) => (
                                                            <TableRow key={entry.entryId}
                                                                      className="border-b-gray-200">
                                                                <TableCell className="font-mono text-xs">
                                                                    {entry.attendee.identity}
                                                                </TableCell>
                                                                <TableCell className="font-medium">
                                                                    {entry.attendee.lastName}
                                                                </TableCell>
                                                                <TableCell>
                                                                    {entry.attendee.firstName}
                                                                </TableCell>
                                                                <TableCell
                                                                    className="text-right text-gray-500 text-sm">
                                                                    {format(new Date(entry.scanTimestamp), "h:mm:ss a")}
                                                                </TableCell>
                                                            </TableRow>
                                                        ))
                                                    ) : (
                                                        <TableRow>
                                                            <TableCell colSpan={4}
                                                                       className="h-24 text-center text-gray-500">
                                                                No arrivals recorded for this event yet.
                                                            </TableCell>
                                                        </TableRow>
                                                    )}
                                                </TableBody>
                                            </Table>

                                            <footer className="mt-8 flex justify-between text-xs text-gray-400">
                                                <span>{organization?.name ?? ''}</span>
                                                <span>Report generated on: {generationDate}</span>
                                            </footer>
                                        </Card>
                                    )}
                                </>
                            ) : (
                                <div
                                    className="flex flex-col h-96 items-center justify-center rounded-lg text-center border-2 border-dashed">
                                    <IconReportAnalytics className="h-16 w-16 text-muted-foreground mb-4"
                                                         stroke={1.5}/>
                                    <h2 className="text-xl font-semibold">Select an Event to Generate a Report</h2>
                                    <p className="text-muted-foreground mt-2">
                                        Choose an event to view its official arrivals report.
                                    </p>
                                </div>
                            )}
                        </div>
                    </div>
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}
