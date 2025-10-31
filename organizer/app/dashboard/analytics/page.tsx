"use client";

import * as React from "react";
import {useMemo, useRef, useState} from "react";
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
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {Separator} from "@/components/ui/separator";

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
        totalCheckedIn,
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

    const EmptyState = () => {
        let title = "Select an Event and Field";
        let description = "Choose an event and a field to break down your checked-in attendee data.";

        if (selectedEventId && selectedGroupBy) {
            if (totalCheckedIn === 0) {
                title = "No Check-in Data Found";
                description = "This event has no check-in records to analyze.";
            } else {
                title = "No Data for this Field";
                description = `No checked-in attendees have a value for the "${selectedGroupBy}" field.`;
            }
        }

        return (
            <div className="flex flex-col h-[350px] items-center justify-center rounded-lg text-center">
                <IconChartDonut className="h-16 w-16 text-muted-foreground mb-4"/>
                <h2 className="text-xl font-semibold">{title}</h2>
                <p className="text-muted-foreground mt-2">{description}</p>
            </div>
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
                            {(!selectedEventId || !selectedGroupBy) ? <EmptyState/> : (
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
                                        {isLoadingBreakdown ?
                                            <Skeleton className="h-[500px] w-full"/> : breakdown.length === 0 ?
                                                <EmptyState/> : (
                                                    <div className="space-y-8">
                                                        <ChartContainer config={chartConfig}
                                                                        className="h-[350px] w-full">
                                                            <BarChart accessibilityLayer data={breakdown} margin={{
                                                                top: 20,
                                                                right: 20,
                                                                bottom: 20,
                                                                left: 20
                                                            }}>
                                                                <CartesianGrid vertical={false}/>
                                                                <XAxis dataKey="groupName" tickLine={false}
                                                                       tickMargin={10} axisLine={false} angle={-45}
                                                                       textAnchor="end" height={60}/>
                                                                <YAxis allowDecimals={false}/>
                                                                <ChartTooltip
                                                                    cursor={false}
                                                                    content={
                                                                        <ChartTooltipContent
                                                                            formatter={(value, name, item) => (
                                                                                <div className="flex flex-col">
                                                                                    <span
                                                                                        className="text-xs text-muted-foreground">{item.payload.groupName}</span>
                                                                                    <span>Attendees: {value} ({totalCheckedIn > 0 ? ((Number(value) / totalCheckedIn) * 100).toFixed(1) : 0}%)</span>
                                                                                </div>
                                                                            )}
                                                                            indicator="dot"
                                                                        />
                                                                    }
                                                                />
                                                                <Bar dataKey="count" fill="var(--color-count)"
                                                                     radius={4}/>
                                                            </BarChart>
                                                        </ChartContainer>

                                                        <Separator/>

                                                        <div>
                                                            <h3 className="text-md font-semibold mb-2">Data Table</h3>
                                                            <div className="rounded-md border">
                                                                <Table>
                                                                    <TableHeader>
                                                                        <TableRow>
                                                                            <TableHead>{selectedGroupBy}</TableHead>
                                                                            <TableHead
                                                                                className="text-right">Count</TableHead>
                                                                            <TableHead
                                                                                className="text-right">Percentage</TableHead>
                                                                        </TableRow>
                                                                    </TableHeader>
                                                                    <TableBody>
                                                                        {breakdown.map((item) => (
                                                                            <TableRow key={item.groupName}>
                                                                                <TableCell
                                                                                    className="font-medium">{item.groupName}</TableCell>
                                                                                <TableCell
                                                                                    className="text-right">{item.count}</TableCell>
                                                                                <TableCell
                                                                                    className="text-right text-muted-foreground">
                                                                                    {totalCheckedIn > 0 ? ((item.count / totalCheckedIn) * 100).toFixed(1) : 0}%
                                                                                </TableCell>
                                                                            </TableRow>
                                                                        ))}
                                                                    </TableBody>
                                                                </Table>
                                                            </div>
                                                        </div>
                                                    </div>
                                                )}
                                    </CardContent>
                                </Card>
                            )}
                        </div>
                    </div>
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}
