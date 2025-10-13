"use client";

import * as React from "react";
import {useMemo, useState} from "react";
import {AppSidebar} from "@/components/layout/app-sidebar";
import {SiteHeader} from "@/components/layout/site-header";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {useReports} from "@/hooks/use-reports";
import {Bar, BarChart, CartesianGrid, XAxis, YAxis} from "recharts";
import {ChartConfig, ChartContainer, ChartTooltip, ChartTooltipContent} from "@/components/ui/chart";
import {Skeleton} from "@/components/ui/skeleton";
import {IconChartBar} from "@tabler/icons-react";

export default function ReportsPage() {
    const [selectedEventId, setSelectedEventId] = useState<number | null>(null);
    const {events, isLoadingEvents, report, isLoadingReport} = useReports(selectedEventId);

    const chartConfig = useMemo(() => ({
        checkIns: {
            label: "Check-ins",
            color: "hsl(var(--primary))",
        },
    }) satisfies ChartConfig, []);

    const chartData = useMemo(() => {
        if (!report?.checkInsByDate) return [];
        return Object.entries(report.checkInsByDate).map(([date, count]) => ({
            date: new Date(date).toLocaleDateString('en-US', {month: 'short', day: 'numeric'}),
            checkIns: count,
        }));
    }, [report]);

    const renderReportContent = () => {
        if (!selectedEventId) {
            return (
                <div className="flex flex-col h-96 items-center justify-center rounded-lg border-2 border-dashed">
                    <IconChartBar className="h-16 w-16 text-muted-foreground mb-4"/>
                    <h2 className="text-xl font-semibold">Select an Event to Generate a Report</h2>
                    <p className="text-muted-foreground mt-2">Choose an event from the dropdown above to view its
                        attendance summary.</p>
                </div>
            );
        }

        if (isLoadingReport) {
            return (
                <div className="space-y-6">
                    <Skeleton className="h-8 w-1/2 mb-4"/>
                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                        <Skeleton className="h-28 w-full"/>
                        <Skeleton className="h-28 w-full"/>
                        <Skeleton className="h-28 w-full"/>
                    </div>
                    <Card>
                        <CardHeader>
                            <Skeleton className="h-6 w-1/4"/>
                            <Skeleton className="h-4 w-1/2"/>
                        </CardHeader>
                        <CardContent>
                            <Skeleton className="h-64 w-full"/>
                        </CardContent>
                    </Card>
                </div>
            );
        }

        if (!report) {
            return <div className="text-center text-muted-foreground">Could not load report data.</div>;
        }

        if (report.totalRegistered === 0) {
            return (
                <div className="flex flex-col h-96 items-center justify-center rounded-lg border-2 border-dashed">
                    <IconChartBar className="h-16 w-16 text-muted-foreground mb-4"/>
                    <h2 className="text-xl font-semibold">No Attendees Registered</h2>
                    <p className="text-muted-foreground mt-2">There are no attendees registered for this event to
                        generate a report.</p>
                </div>
            );
        }

        return (
            <div className="space-y-6 animate-in fade-in-50">
                <h2 className="text-2xl font-bold tracking-tight">{report.eventName} - Report Summary</h2>
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                    <Card>
                        <CardHeader>
                            <CardDescription>Total Registered</CardDescription>
                            <CardTitle className="text-3xl">{report.totalRegistered.toLocaleString()}</CardTitle>
                        </CardHeader>
                    </Card>
                    <Card>
                        <CardHeader>
                            <CardDescription>Total Checked-In</CardDescription>
                            <CardTitle className="text-3xl">{report.totalCheckedIn.toLocaleString()}</CardTitle>
                        </CardHeader>
                    </Card>
                    <Card>
                        <CardHeader>
                            <CardDescription>Attendance Rate</CardDescription>
                            <CardTitle className="text-3xl text-primary">{report.attendanceRate.toFixed(1)}%</CardTitle>
                        </CardHeader>
                    </Card>
                </div>
                <Card>
                    <CardHeader>
                        <CardTitle>Check-ins Per Day</CardTitle>
                        <CardDescription>A summary of attendance traffic throughout the event.</CardDescription>
                    </CardHeader>
                    <CardContent>
                        {chartData.length > 0 ? (
                            <ChartContainer config={chartConfig} className="h-[250px] w-full">
                                <BarChart accessibilityLayer data={chartData}>
                                    <CartesianGrid vertical={false}/>
                                    <XAxis dataKey="date" tickLine={false} tickMargin={10} axisLine={false}/>
                                    <YAxis allowDecimals={false}/>
                                    <ChartTooltip cursor={false} content={<ChartTooltipContent indicator="dot"/>}/>
                                    <Bar dataKey="checkIns" fill="var(--color-checkIns)" radius={4}/>
                                </BarChart>
                            </ChartContainer>
                        ) : (
                            <div className="flex h-[250px] items-center justify-center">
                                <p className="text-muted-foreground">No check-in data available for this event.</p>
                            </div>
                        )}
                    </CardContent>
                </Card>
            </div>
        );
    };


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
                <SiteHeader title="Reports"/>
                <main className="flex-1 p-4 lg:p-6">
                    <div className="w-full max-w-5xl mx-auto space-y-6">
                        {/* The redundant title has been removed, and justify-end aligns the dropdown to the right */}
                        <div className="flex items-center justify-end">
                            {isLoadingEvents ? <Skeleton className="h-10 w-full sm:w-64"/> : (
                                <Select
                                    value={selectedEventId ? String(selectedEventId) : ""}
                                    onValueChange={(value) => setSelectedEventId(Number(value))}
                                >
                                    <SelectTrigger className="w-full sm:w-64">
                                        <SelectValue placeholder="Select an Event"/>
                                    </SelectTrigger>
                                    <SelectContent>
                                        {events.map(event => (
                                            <SelectItem key={event.id} value={String(event.id)}>
                                                {event.eventName}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            )}
                        </div>
                        <div className="pt-4">
                            {renderReportContent()}
                        </div>
                    </div>
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}
