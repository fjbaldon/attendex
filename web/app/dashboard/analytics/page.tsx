"use client";

import * as React from "react";
import {useMemo, useRef, useState} from "react";
import dynamic from "next/dynamic";
import {AppSidebar} from "@/components/layout/app-sidebar";
import {SiteHeader} from "@/components/layout/site-header";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {useEvents} from "@/hooks/use-events";
import {useAnalytics} from "@/hooks/use-analytics";
import {Bar, CartesianGrid, XAxis, YAxis} from "recharts";
import {ChartContainer, ChartTooltip, ChartTooltipContent} from "@/components/ui/chart";
import {Skeleton} from "@/components/ui/skeleton";
import {IconChartBar, IconChartDonut, IconFileDownload, IconTable} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {ToggleGroup, ToggleGroupItem} from "@/components/ui/toggle-group";
import {toast} from "sonner";
import {useAttributes} from "@/hooks/use-attributes";

const BarChart = dynamic(() => import("recharts").then(mod => mod.BarChart), {
    ssr: false,
    loading: () => <Skeleton className="h-[350px] w-full"/>,
});

const PieChartView = dynamic(() => import("./pie-chart-view").then(mod => mod.PieChartView), {
    ssr: false,
    loading: () => <Skeleton className="h-[350px] w-full"/>,
});

type ChartType = 'bar' | 'pie' | 'table';

export default function AnalyticsPage() {
    const [selectedEventId, setSelectedEventId] = useState<string>("");
    const [selectedAttribute, setSelectedAttribute] = useState<string>("");
    const [chartType, setChartType] = useState<ChartType>('bar');
    const [isExporting, setIsExporting] = useState(false);
    const analyticsContentRef = useRef<HTMLDivElement>(null);

    const {eventsData, isLoadingEvents} = useEvents(0, 9999);
    const events = eventsData?.content ?? [];

    const {definitions: attributes, isLoading: isLoadingAttributes} = useAttributes();
    const {breakdown, isLoadingBreakdown} = useAnalytics(selectedEventId, selectedAttribute);

    const totalEntries = useMemo(() => {
        return breakdown.reduce((sum, item) => sum + item.count, 0);
    }, [breakdown]);

    const chartConfig = useMemo(() => ({
        count: {
            label: "Attendees",
            color: "hsl(var(--primary))",
        },
    }), []);

    const selectedEvent = events.find(e => String(e.id) === selectedEventId);

    const handleExport = async () => {
        if (!analyticsContentRef.current || !selectedEventId || !selectedAttribute) {
            toast.error("Please select an event and an attribute to export.");
            return;
        }

        setIsExporting(true);
        toast.info("Preparing PDF...", {description: "This may take a moment."});

        try {
            const {exportToPdf} = await import("@/lib/pdf-exporter");
            const eventName = selectedEvent ? selectedEvent.name.replace(/ /g, '_') : 'analytics';
            const fileName = `analytics-${eventName}-by-${selectedAttribute}`;
            await exportToPdf(analyticsContentRef.current, fileName);
        } catch (error) {
            console.error("Failed to export PDF:", error);
            toast.error("Export Failed", {description: "An unexpected error occurred while generating the PDF."});
        } finally {
            setIsExporting(false);
        }
    };

    const EmptyState = () => {
        let title = "Select an Event and Attribute";
        let description = "Choose an event and an attribute to break down your checked-in attendee data.";

        if (selectedEventId && selectedAttribute) {
            if (totalEntries === 0) {
                title = "No Entry Data Found";
                description = "This event has no scan records to analyze.";
            } else {
                title = "No Data for this Attribute";
                description = `No checked-in attendees have a value for the "${selectedAttribute}" attribute.`;
            }
        }

        return (
            <div
                className="flex flex-col h-96 items-center justify-center rounded-lg text-center border-2 border-dashed">
                <IconChartDonut className="h-16 w-16 text-muted-foreground mb-4"/>
                <h2 className="text-xl font-semibold">{title}</h2>
                <p className="text-muted-foreground mt-2">{description}</p>
            </div>
        );
    };

    const DataTable = () => (
        <div className="rounded-md border">
            <Table>
                <TableHeader>
                    <TableRow>
                        <TableHead>{selectedAttribute}</TableHead>
                        <TableHead className="text-right">Count</TableHead>
                        <TableHead className="text-right">Percentage</TableHead>
                    </TableRow>
                </TableHeader>
                <TableBody>
                    {breakdown.map((item) => (
                        <TableRow key={item.value}>
                            <TableCell className="font-medium">{item.value}</TableCell>
                            <TableCell className="text-right">{item.count}</TableCell>
                            <TableCell className="text-right text-muted-foreground">
                                {totalEntries > 0 ? ((item.count / totalEntries) * 100).toFixed(1) : 0}%
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </div>
    );

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
                                                            value={String(event.id)}>{event.name}</SelectItem>
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

                        {selectedEventId && (
                            <div className="rounded-lg border bg-card p-4">
                                <div className="flex items-center gap-4">
                                    <p className="text-sm font-medium text-muted-foreground">Breakdown by:</p>
                                    {isLoadingAttributes ? <Skeleton className="h-9 w-full sm:w-64"/> : (
                                        <Select value={selectedAttribute} onValueChange={setSelectedAttribute}
                                                disabled={!attributes.length}>
                                            <SelectTrigger className="w-full sm:w-64">
                                                <SelectValue placeholder="Select an Attribute..."/>
                                            </SelectTrigger>
                                            <SelectContent>
                                                {attributes.map(attr => (
                                                    <SelectItem key={attr.id} value={attr.name}>{attr.name}</SelectItem>
                                                ))}
                                            </SelectContent>
                                        </Select>
                                    )}
                                </div>
                            </div>
                        )}

                        <div className="pt-2">
                            {(!selectedEventId || !selectedAttribute) ? <EmptyState/> : (
                                <Card>
                                    <CardHeader>
                                        <div>
                                            <CardTitle>Attribute Breakdown</CardTitle>
                                            <CardDescription>
                                                Visualizing the distribution of {totalEntries} entries
                                                for &quot;{selectedEvent?.name}&quot;.
                                            </CardDescription>
                                        </div>
                                        <CardAction>
                                            <ToggleGroup
                                                type="single"
                                                value={chartType}
                                                onValueChange={(value: ChartType) => {
                                                    if (value) setChartType(value);
                                                }}
                                                variant="outline"
                                            >
                                                <ToggleGroupItem value="bar"
                                                                 aria-label="Bar chart"><IconChartBar/></ToggleGroupItem>
                                                <ToggleGroupItem value="pie"
                                                                 aria-label="Pie chart"><IconChartDonut/></ToggleGroupItem>
                                                <ToggleGroupItem value="table"
                                                                 aria-label="Data table"><IconTable/></ToggleGroupItem>
                                            </ToggleGroup>
                                        </CardAction>
                                    </CardHeader>
                                    <CardContent className="p-6 pt-0">
                                        <div ref={analyticsContentRef} className="bg-white text-black py-4">
                                            {isLoadingBreakdown ?
                                                <Skeleton className="h-[350px] w-full"/> : breakdown.length === 0 ?
                                                    <EmptyState/> : (
                                                        <>
                                                            {chartType === 'bar' && (
                                                                <ChartContainer config={chartConfig}
                                                                                className="h-[350px] w-full">
                                                                    <BarChart data={breakdown}>
                                                                        <CartesianGrid vertical={false}/>
                                                                        <XAxis dataKey="value" tickLine={false}
                                                                               tickMargin={10} axisLine={false}
                                                                               angle={-45}
                                                                               textAnchor="end" height={60}
                                                                               stroke="black"/>
                                                                        <YAxis allowDecimals={false} stroke="black"/>
                                                                        <ChartTooltip
                                                                            cursor={false}
                                                                            content={
                                                                                <ChartTooltipContent
                                                                                    formatter={(value) => `${value} (${totalEntries > 0 ? ((Number(value) / totalEntries) * 100).toFixed(1) : 0}%)`}
                                                                                />
                                                                            }
                                                                        />
                                                                        <Bar dataKey="count" fill="var(--color-count)"
                                                                             radius={4}/>
                                                                    </BarChart>
                                                                </ChartContainer>
                                                            )}
                                                            {chartType === 'pie' &&
                                                                <PieChartView data={breakdown} total={totalEntries}/>}
                                                            {chartType === 'table' && <DataTable/>}
                                                        </>
                                                    )}
                                        </div>
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
