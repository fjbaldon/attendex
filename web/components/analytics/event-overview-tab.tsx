"use client";

import React, {useState} from "react"; // Import useState
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Skeleton} from "@/components/ui/skeleton";
import {IconUserMinus, IconUsers} from "@tabler/icons-react";
import {ChartContainer, ChartTooltip, ChartTooltipContent} from "@/components/ui/chart";
import {Bar, BarChart, CartesianGrid, XAxis} from "recharts";
import {CohortAnalysis} from "./cohort-analysis";
import {AnalyticsBreakdownItem, EventStats} from "@/types";
import {KpiCard} from "./kpi-card";

interface EventOverviewTabProps {
    stats?: EventStats;
    breakdown: AnalyticsBreakdownItem[];
    attributes: { id: number; name: string }[];
    selectedAttribute: string;
    setSelectedAttribute: (val: string) => void;
    isLoadingStats: boolean;
    isLoadingAttributes: boolean;
    isLoadingBreakdown: boolean;
    eventId: string;
}

export function EventOverviewTab({
                                     stats,
                                     breakdown,
                                     attributes,
                                     selectedAttribute,
                                     setSelectedAttribute,
                                     isLoadingStats,
                                     isLoadingAttributes,
                                     isLoadingBreakdown,
                                     eventId
                                 }: EventOverviewTabProps) {

    // FIX: Add local state for the Cohort Analysis component
    const [cohortFilters, setCohortFilters] = useState<Record<string, string>>({});

    const absentCount = Math.max(0, (stats?.totalRoster || 0) - (stats?.totalScans || 0));
    const turnoutPercentage = stats?.attendanceRate ? stats.attendanceRate.toFixed(1) : "0.0";

    return (
        <div className="space-y-6 animate-in fade-in slide-in-from-bottom-2 duration-500">
            {/* ... KPIs Section (Unchanged) ... */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <KpiCard
                    title="Turnout Rate"
                    icon={IconUsers}
                    value={`${turnoutPercentage}%`}
                    subtext={`${stats?.totalScans ?? 0} of ${stats?.totalRoster ?? 0} attended`}
                    loading={isLoadingStats}
                    color="text-primary"
                />
                <KpiCard
                    title="No-Shows"
                    icon={IconUserMinus}
                    value={absentCount}
                    subtext="Registered but absent"
                    loading={isLoadingStats}
                    color="text-muted-foreground"
                />
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* ... Audience Composition Card (Unchanged) ... */}
                <Card className="shadow-sm flex flex-col h-full min-h-[400px]">
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <div className="space-y-1">
                            <CardTitle>Verified Attendees Breakdown</CardTitle>
                            <p className="text-sm text-muted-foreground">Total scans grouped by attribute.</p>
                        </div>
                        {isLoadingAttributes ? <Skeleton className="h-8 w-32"/> : (
                            <Select value={selectedAttribute} onValueChange={setSelectedAttribute} disabled={attributes.length === 0}>
                                <SelectTrigger className="h-8 w-[140px] text-xs">
                                    <SelectValue placeholder="Attribute"/>
                                </SelectTrigger>
                                <SelectContent>
                                    {attributes.map(attr => <SelectItem key={attr.id} value={attr.name}>{attr.name}</SelectItem>)}
                                </SelectContent>
                            </Select>
                        )}
                    </CardHeader>
                    <CardContent className="flex-1 flex flex-col justify-end">
                        {!selectedAttribute ? (
                            <div className="h-full flex items-center justify-center text-muted-foreground border-2 border-dashed rounded-lg bg-muted/5 m-4">
                                Select an attribute to view composition
                            </div>
                        ) : isLoadingBreakdown ? <Skeleton className="h-[250px] w-full"/> : (
                            <ChartContainer config={{count: {label: "Attendees", color: "hsl(var(--chart-1))"}}} className="h-[300px] w-full mt-auto">
                                <BarChart data={breakdown} margin={{top: 10, bottom: 0}}>
                                    <CartesianGrid vertical={false} strokeDasharray="3 3"/>
                                    <XAxis dataKey="value" tickLine={false} axisLine={false} tickMargin={10} fontSize={12} />
                                    <ChartTooltip content={<ChartTooltipContent/>}/>
                                    <Bar dataKey="count" fill="var(--color-count)" radius={[4, 4, 0, 0]} barSize={40} />
                                </BarChart>
                            </ChartContainer>
                        )}
                    </CardContent>
                </Card>

                {/* RIGHT: Cohort Analysis */}
                <div className="h-full min-h-[400px]">
                    {/* FIX: Passed required props */}
                    <CohortAnalysis
                        eventId={eventId}
                        filters={cohortFilters}
                        onFiltersChange={setCohortFilters}
                    />
                </div>
            </div>
        </div>
    );
}
