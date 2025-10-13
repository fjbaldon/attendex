"use client";

import * as React from "react";
import {useMemo, useState} from "react";
import {AppSidebar} from "@/components/layout/app-sidebar";
import {SiteHeader} from "@/components/layout/site-header";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {useEvents} from "@/hooks/use-events";
import {useAnalytics} from "@/hooks/use-analytics";
import {Bar, BarChart, CartesianGrid, XAxis, YAxis} from "recharts";
import {ChartConfig, ChartContainer, ChartTooltip, ChartTooltipContent} from "@/components/ui/chart";
import {Separator} from "@/components/ui/separator";
import {Skeleton} from "@/components/ui/skeleton";

export default function AnalyticsPage() {
    const [selectedEventId, setSelectedEventId] = useState<string>("");
    const [selectedGroupBy, setSelectedGroupBy] = useState<string>("");

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

    const renderChartContent = () => {
        if (!selectedEventId || !selectedGroupBy) {
            return <div className="text-center text-muted-foreground">Please select an event and a field to group
                by.</div>;
        }
        if (isLoadingBreakdown) {
            return <Skeleton className="h-[250px] w-full"/>;
        }
        if (breakdown.length === 0) {
            return <div className="text-center text-muted-foreground">No attendance data found for this
                selection.</div>;
        }
        return (
            <ChartContainer config={chartConfig} className="h-[250px] w-full">
                <BarChart accessibilityLayer data={breakdown}>
                    <CartesianGrid vertical={false}/>
                    <XAxis
                        dataKey="groupName"
                        tickLine={false}
                        tickMargin={10}
                        axisLine={false}
                    />
                    <YAxis/>
                    <ChartTooltip
                        cursor={false}
                        content={<ChartTooltipContent indicator="dot"/>}
                    />
                    <Bar dataKey="count" fill="var(--color-count)" radius={4}/>
                </BarChart>
            </ChartContainer>
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
                <SiteHeader title="Analytics"/>
                <main className="flex flex-1 flex-col gap-4 p-4 lg:gap-6 lg:p-6">
                    <div className="w-full max-w-4xl mx-auto space-y-6">
                        <div className="flex items-center">
                            <div>
                                <h1 className="text-lg font-semibold md:text-2xl">Attendance Analytics</h1>
                                <p className="text-muted-foreground text-sm">
                                    Explore attendance data by breaking it down with your custom fields.
                                </p>
                            </div>
                        </div>

                        <Card>
                            <CardHeader>
                                <CardTitle>Attendance Breakdown</CardTitle>
                                <CardDescription>
                                    Select an event and a custom field to see how attendees are distributed.
                                </CardDescription>
                            </CardHeader>
                            <CardContent className="space-y-6">
                                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                                    {isLoadingEvents ? <Skeleton className="h-10 w-full"/> : (
                                        <Select value={selectedEventId} onValueChange={setSelectedEventId}>
                                            <SelectTrigger>
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
                                    {isLoadingCustomFields ? <Skeleton className="h-10 w-full"/> : (
                                        <Select value={selectedGroupBy} onValueChange={setSelectedGroupBy}
                                                disabled={!customFields.length}>
                                            <SelectTrigger>
                                                <SelectValue placeholder="Group By..."/>
                                            </SelectTrigger>
                                            <SelectContent>
                                                {customFields.map(field => (
                                                    <SelectItem key={field} value={field}>
                                                        {field}
                                                    </SelectItem>
                                                ))}
                                            </SelectContent>
                                        </Select>
                                    )}
                                </div>
                                <Separator/>
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
