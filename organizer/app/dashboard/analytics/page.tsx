"use client";

import * as React from "react";
import {useMemo, useState, useRef} from "react";
import {AppSidebar} from "@/components/layout/app-sidebar";
import {SiteHeader} from "@/components/layout/site-header";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {useEvents} from "@/hooks/use-events";
import {useAnalytics} from "@/hooks/use-analytics";
import {Bar, BarChart, CartesianGrid, XAxis, YAxis} from "recharts";
import {ChartConfig, ChartContainer, ChartTooltip, ChartTooltipContent} from "@/components/ui/chart";
import {Skeleton} from "@/components/ui/skeleton";
import {IconChartDonut, IconFileDownload} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";
import {exportToPdf} from "@/lib/pdf-exporter";

export default function AnalyticsPage() {
    const [selectedEventId, setSelectedEventId] = useState<string>("");
    const [selectedGroupBy, setSelectedGroupBy] = useState<string>("");
    const [isExporting, setIsExporting] = useState(false);
    const analyticsContentRef = useRef<HTMLDivElement>(null);

    const {events, isLoadingEvents} = useEvents();
    const {
        customFields,
        isLoadingCustomFields,
        breakdown,
        isLoadingBreakdown
    } = useAnalytics(selectedEventId, selectedGroupBy);

    const chartConfig = useMemo(() => ({
        count: {
            label: "Attendees",
            color: "hsl(var(--primary))",
        },
    }) satisfies ChartConfig, []);

    const selectedEvent = events.find(e => String(e.id) === selectedEventId);

    const handleExport = async () => {
        if (!analyticsContentRef.current || !selectedEventId || !selectedGroupBy) return;

        setIsExporting(true);
        const eventName = selectedEvent ? selectedEvent.eventName.replace(/ /g, '_') : 'analytics';
        const fileName = `analytics-${eventName}-by-${selectedGroupBy}`;

        try {
            await exportToPdf(analyticsContentRef.current, fileName);
        } catch (error) {
            console.error("Failed to export PDF:", error);
        } finally {
            setIsExporting(false);
        }
    };

    return (
        <SidebarProvider style={{
            "--sidebar-width": "calc(var(--spacing) * 72)",
            "--header-height": "calc(var(--spacing) * 12)"
        } as React.CSSProperties}>
            <AppSidebar variant="inset"/>
            <SidebarInset>
                <SiteHeader title="Analytics"/>
                <main className="flex-1 p-4 lg:p-6">
                    <div className="w-full max-w-6xl mx-auto space-y-6">
                        {/* Header Section */}
                        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-start">
                            <div className="flex flex-shrink-0 items-center gap-2">
                                {isLoadingEvents ? <Skeleton className="h-9 w-full sm:w-64"/> : (
                                    <Select value={selectedEventId} onValueChange={setSelectedEventId}>
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
                                <Button
                                    variant="outline"
                                    onClick={handleExport}
                                    disabled={!breakdown || breakdown.length === 0 || isExporting || isLoadingBreakdown}
                                >
                                    <IconFileDownload className="mr-2 h-4 w-4" stroke={1.5}/>
                                    {isExporting ? 'Exporting...' : 'Export PDF'}
                                </Button>
                            </div>
                        </div>

                        {/* Breakdown Section */}
                        {selectedEventId && (
                            <div className="rounded-lg border bg-card p-4">
                                <div className="flex items-center gap-4">
                                    <p className="text-sm font-medium text-muted-foreground">Breakdown by:</p>
                                    {isLoadingCustomFields ? <Skeleton className="h-9 w-full sm:w-64"/> : (
                                        <Select value={selectedGroupBy} onValueChange={setSelectedGroupBy}
                                                disabled={!customFields.length}>
                                            <SelectTrigger className="w-full sm:w-64">
                                                <SelectValue placeholder="Select a Field..."/>
                                            </SelectTrigger>
                                            <SelectContent>
                                                {customFields.map(field => (
                                                    <SelectItem key={field} value={field}>{field}</SelectItem>
                                                ))}
                                            </SelectContent>
                                        </Select>
                                    )}
                                </div>
                            </div>
                        )}

                        {/* Chart / Empty State Section */}
                        <div className="pt-2">
                            {selectedEventId && selectedGroupBy ? (
                                <Card ref={analyticsContentRef}>
                                    <CardHeader>
                                        <CardTitle>Checked-in Breakdown</CardTitle>
                                        <CardDescription>
                                            Visualizing the distribution of checked-in attendees
                                            for &quot;{selectedEvent?.eventName}&quot; grouped
                                            by &quot;{selectedGroupBy}&quot;.
                                        </CardDescription>
                                    </CardHeader>
                                    <CardContent>
                                        {isLoadingBreakdown ? (
                                            <Skeleton className="h-[350px] w-full"/>
                                        ) : breakdown.length === 0 ? (
                                            <div
                                                className="flex flex-col h-[350px] items-center justify-center rounded-lg">
                                                <IconChartDonut className="h-16 w-16 text-muted-foreground mb-4"/>
                                                <h2 className="text-xl font-semibold">No Data to Display</h2>
                                                <p className="text-muted-foreground mt-2">No checked-in records were
                                                    found for this combination.</p>
                                            </div>
                                        ) : (
                                            <ChartContainer config={chartConfig} className="h-[350px] w-full">
                                                <BarChart accessibilityLayer data={breakdown}
                                                          margin={{top: 20, right: 20, bottom: 20, left: 20}}>
                                                    <CartesianGrid vertical={false}/>
                                                    <XAxis dataKey="groupName" tickLine={false} tickMargin={10}
                                                           axisLine={false} angle={-45} textAnchor="end" height={60}/>
                                                    <YAxis allowDecimals={false}/>
                                                    <ChartTooltip cursor={false}
                                                                  content={<ChartTooltipContent indicator="dot"/>}/>
                                                    <Bar dataKey="count" fill="var(--color-count)" radius={4}/>
                                                </BarChart>
                                            </ChartContainer>
                                        )}
                                    </CardContent>
                                </Card>
                            ) : (
                                <div
                                    className="flex flex-col h-96 items-center justify-center rounded-lg border-2 border-dashed text-center">
                                    <IconChartDonut className="h-16 w-16 text-muted-foreground mb-4"/>
                                    <h2 className="text-xl font-semibold">Select an Event and Field</h2>
                                    <p className="text-muted-foreground mt-2 text-center">
                                        Choose an event and a field to break down your checked-in attendee data.
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
