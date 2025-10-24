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

    const handleExport = async () => {
        if (!analyticsContentRef.current || !selectedEventId || !selectedGroupBy) {
            return;
        }

        setIsExporting(true);
        const selectedEvent = events.find(e => String(e.id) === selectedEventId);
        const eventName = selectedEvent ? selectedEvent.eventName.replace(/ /g, '_') : 'analytics';
        const fileName = `analytics-${eventName}-by-${selectedGroupBy}.pdf`;

        try {
            await exportToPdf(analyticsContentRef.current, fileName);
        } catch (error) {
            console.error("Failed to export PDF:", error);
        } finally {
            setIsExporting(false);
        }
    };

    const renderChartContent = () => {
        if (!selectedEventId || !selectedGroupBy) {
            return (
                <div className="flex flex-col h-80 items-center justify-center rounded-lg border-2 border-dashed">
                    <IconChartDonut className="h-16 w-16 text-muted-foreground mb-4"/>
                    <h2 className="text-xl font-semibold">Select an Event and Field</h2>
                    <p className="text-muted-foreground mt-2 text-center">Choose an event and a custom field to break
                        down attendance data.</p>
                </div>
            );
        }
        if (isLoadingBreakdown) {
            return <Skeleton className="h-[320px] w-full"/>;
        }
        if (breakdown.length === 0) {
            return (
                <div className="flex flex-col h-80 items-center justify-center rounded-lg border-2 border-dashed">
                    <IconChartDonut className="h-16 w-16 text-muted-foreground mb-4"/>
                    <h2 className="text-xl font-semibold">No Data to Display</h2>
                    <p className="text-muted-foreground mt-2">No attendance records were found for this combination.</p>
                </div>
            );
        }
        return (
            <ChartContainer config={chartConfig} className="h-[320px] w-full">
                <BarChart accessibilityLayer data={breakdown} margin={{top: 20, right: 20, bottom: 20, left: 20}}>
                    <CartesianGrid vertical={false}/>
                    <XAxis dataKey="groupName" tickLine={false} tickMargin={10} axisLine={false} angle={-45}
                           textAnchor="end" height={60}/>
                    <YAxis allowDecimals={false}/>
                    <ChartTooltip cursor={false} content={<ChartTooltipContent indicator="dot"/>}/>
                    <Bar dataKey="count" fill="var(--color-count)" radius={4}/>
                </BarChart>
            </ChartContainer>
        );
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
                    <div className="w-full max-w-4xl mx-auto space-y-6">
                        <div className="flex items-center justify-between">
                            <div>
                                <h1 className="text-lg font-semibold md:text-2xl">Custom Field Analytics</h1>
                                <p className="text-muted-foreground text-sm">Explore attendance data by breaking it down
                                    with your custom fields.</p>
                            </div>
                            <Button
                                variant="outline"
                                onClick={handleExport}
                                disabled={!breakdown || breakdown.length === 0 || isExporting || isLoadingBreakdown}
                            >
                                <IconFileDownload className="mr-2 h-4 w-4"/>
                                {isExporting ? 'Exporting...' : 'Export PDF'}
                            </Button>
                        </div>

                        <Card ref={analyticsContentRef}>
                            <CardHeader>
                                <CardTitle>Attendance Breakdown</CardTitle>
                                <CardDescription>Select an event and a custom field to see how attendees are
                                    distributed.</CardDescription>
                            </CardHeader>
                            <CardContent className="space-y-6">
                                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                                    {isLoadingEvents ? <Skeleton className="h-10 w-full"/> : (
                                        <Select value={selectedEventId} onValueChange={setSelectedEventId}>
                                            <SelectTrigger><SelectValue
                                                placeholder="1. Select an Event"/></SelectTrigger>
                                            <SelectContent>
                                                {events.map(event => (<SelectItem key={event.id}
                                                                                  value={String(event.id)}>{event.eventName}</SelectItem>))}
                                            </SelectContent>
                                        </Select>
                                    )}
                                    {isLoadingCustomFields ? <Skeleton className="h-10 w-full"/> : (
                                        <Select value={selectedGroupBy} onValueChange={setSelectedGroupBy}
                                                disabled={!customFields.length || !selectedEventId}>
                                            <SelectTrigger><SelectValue placeholder="2. Group By..."/></SelectTrigger>
                                            <SelectContent>
                                                {customFields.map(field => (
                                                    <SelectItem key={field} value={field}>{field}</SelectItem>))}
                                            </SelectContent>
                                        </Select>
                                    )}
                                </div>
                                <div className="pt-4">
                                    {renderChartContent()}
                                </div>
                            </CardContent>
                        </Card>
                    </div>
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}
