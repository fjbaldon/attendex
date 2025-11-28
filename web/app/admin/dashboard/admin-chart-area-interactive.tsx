"use client";

import * as React from "react";
import {Area, AreaChart, CartesianGrid, XAxis} from "recharts";
import {Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {ChartConfig, ChartContainer, ChartTooltip, ChartTooltipContent} from "@/components/ui/chart";
import {ToggleGroup, ToggleGroupItem} from "@/components/ui/toggle-group";
import {DailyRegistration} from "@/types";
import {Skeleton} from "@/components/ui/skeleton";

const chartConfig = {
    registrations: {label: "Registrations", color: "hsl(var(--primary))"},
} satisfies ChartConfig;

interface ChartAreaInteractiveProps {
    data: DailyRegistration[];
    isLoading: boolean;
    timeRange: string;
    setTimeRange: (range: string) => void;
}

export function AdminChartAreaInteractive({data, isLoading, timeRange, setTimeRange}: ChartAreaInteractiveProps) {
    const chartData = React.useMemo(() => {
        return data.map(item => ({
            date: item.date,
            registrations: item.count,
        }));
    }, [data]);

    return (
        <Card className="@container/card">
            <CardHeader>
                {isLoading ? (
                    <div className="space-y-2">
                        <Skeleton className="h-6 w-48" />
                        <Skeleton className="h-4 w-64" />
                    </div>
                ) : (
                    <>
                        <CardTitle>New Organization Registrations</CardTitle>
                        <CardDescription>A summary of daily sign-ups on the platform.</CardDescription>
                    </>
                )}

                <CardAction>
                    {isLoading ? (
                        <Skeleton className="h-9 w-[280px] hidden @[767px]/card:block rounded-md" />
                    ) : (
                        <ToggleGroup
                            type="single"
                            value={timeRange}
                            onValueChange={(value) => value && setTimeRange(value)}
                            variant="outline"
                            className="hidden *:data-[slot=toggle-group-item]:!px-4 @[767px]/card:flex"
                        >
                            <ToggleGroupItem value="90d">Last 90 days</ToggleGroupItem>
                            <ToggleGroupItem value="30d">Last 30 days</ToggleGroupItem>
                            <ToggleGroupItem value="7d">Last 7 days</ToggleGroupItem>
                        </ToggleGroup>
                    )}
                </CardAction>
            </CardHeader>
            <CardContent className="px-2 pt-4 sm:px-6 sm:pt-6">
                {isLoading ? (
                    <Skeleton className="h-[250px] w-full rounded-lg" />
                ) : (
                    <ChartContainer config={chartConfig} className="aspect-auto h-[250px] w-full">
                        <AreaChart data={chartData}>
                            <defs>
                                <linearGradient id="fillRegistrations" x1="0" y1="0" x2="0" y2="1">
                                    <stop offset="5%" stopColor="var(--color-registrations)" stopOpacity={0.8}/>
                                    <stop offset="95%" stopColor="var(--color-registrations)" stopOpacity={0.1}/>
                                </linearGradient>
                            </defs>
                            <CartesianGrid vertical={false}/>
                            <XAxis
                                dataKey="date"
                                tickLine={false}
                                axisLine={false}
                                tickMargin={8}
                                minTickGap={32}
                                tickFormatter={(value) => new Date(value).toLocaleDateString("en-US", {
                                    month: "short",
                                    day: "numeric"
                                })}
                            />
                            <ChartTooltip
                                cursor={false}
                                content={
                                    <ChartTooltipContent
                                        labelFormatter={(value) => new Date(value).toLocaleDateString("en-US", {
                                            month: "short",
                                            day: "numeric",
                                            year: "numeric"
                                        })}
                                        indicator="dot"
                                    />
                                }
                            />
                            <Area dataKey="registrations" type="natural" fill="url(#fillRegistrations)"
                                  stroke="var(--color-registrations)" stackId="a"/>
                        </AreaChart>
                    </ChartContainer>
                )}
            </CardContent>
        </Card>
    );
}
