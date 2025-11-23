"use client"

import * as React from "react"
import {Area, AreaChart, CartesianGrid, XAxis} from "recharts"
import {Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle,} from "@/components/ui/card"
import {ChartConfig, ChartContainer, ChartTooltip, ChartTooltipContent,} from "@/components/ui/chart"
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue,} from "@/components/ui/select"
import {ToggleGroup, ToggleGroupItem,} from "@/components/ui/toggle-group"
import {DailyActivity} from "@/types";
import {Skeleton} from "@/components/ui/skeleton";

const chartConfig = {
    entries: {
        label: "Entries",
        color: "hsl(var(--primary))",
    },
} satisfies ChartConfig

interface ChartAreaInteractiveProps {
    data: DailyActivity[];
    isLoading: boolean;
    timeRange: string;
    setTimeRange: (range: string) => void;
}

export function ChartAreaInteractive({data, isLoading, timeRange, setTimeRange}: ChartAreaInteractiveProps) {
    const chartData = React.useMemo(() => {
        return data.map(item => ({
            date: item.date,
            entries: item.count,
        }));
    }, [data]);

    return (
        <Card className="@container/card">
            <CardHeader>
                <CardTitle>Activity Overview</CardTitle>
                <CardDescription>
                    A summary of daily entry activity across all events.
                </CardDescription>
                <CardAction>
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
                    <Select value={timeRange} onValueChange={setTimeRange}>
                        <SelectTrigger
                            className="flex w-40 **:data-[slot=select-value]:block **:data-[slot=select-value]:truncate @[767px]/card:hidden"
                            size="sm"
                            aria-label="Select a time range"
                        >
                            <SelectValue placeholder="Select a time range"/>
                        </SelectTrigger>
                        <SelectContent className="rounded-xl">
                            <SelectItem value="90d" className="rounded-lg">Last 90 days</SelectItem>
                            <SelectItem value="30d" className="rounded-lg">Last 30 days</SelectItem>
                            <SelectItem value="7d" className="rounded-lg">Last 7 days</SelectItem>
                        </SelectContent>
                    </Select>
                </CardAction>
            </CardHeader>
            <CardContent className="px-2 pt-4 sm:px-6 sm:pt-6">
                {isLoading ? (
                    <Skeleton className="h-[250px] w-full"/>
                ) : (
                    <ChartContainer
                        config={chartConfig}
                        className="aspect-auto h-[250px] w-full"
                    >
                        <AreaChart data={chartData}>
                            <defs>
                                <linearGradient id="fillEntries" x1="0" y1="0" x2="0" y2="1">
                                    <stop offset="5%" stopColor="var(--color-entries)" stopOpacity={0.8}/>
                                    <stop offset="95%" stopColor="var(--color-entries)" stopOpacity={0.1}/>
                                </linearGradient>
                            </defs>
                            <CartesianGrid vertical={false}/>
                            <XAxis
                                dataKey="date"
                                tickLine={false}
                                axisLine={false}
                                tickMargin={8}
                                minTickGap={32}
                                tickFormatter={(value) => {
                                    const date = new Date(value);
                                    return date.toLocaleDateString("en-US", {month: "short", day: "numeric"});
                                }}
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
                            <Area
                                dataKey="entries"
                                type="natural"
                                fill="url(#fillEntries)"
                                stroke="var(--color-entries)"
                                stackId="a"
                            />
                        </AreaChart>
                    </ChartContainer>
                )}
            </CardContent>
        </Card>
    )
}
