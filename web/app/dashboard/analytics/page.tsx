"use client";

import * as React from "react";
import {useState} from "react";
import dynamic from "next/dynamic";
import {AppSidebar} from "@/components/layout/app-sidebar";
import {SiteHeader} from "@/components/layout/site-header";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {useEvents} from "@/hooks/use-events";
import {useAnalytics} from "@/hooks/use-analytics";
import {Bar, CartesianGrid, XAxis, YAxis} from "recharts";
import {ChartContainer, ChartTooltip, ChartTooltipContent} from "@/components/ui/chart";
import {Skeleton} from "@/components/ui/skeleton";
import {IconClock, IconFileDownload, IconScan, IconTrophy, IconUsers} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";
import {toast} from "sonner";
import {format} from "date-fns";
import {useAttributes} from "@/hooks/use-attributes";

const BarChart = dynamic(() => import("recharts").then(mod => mod.BarChart), {ssr: false});

export default function AnalyticsPage() {
    const [selectedEventId, setSelectedEventId] = useState<string>("");
    const [selectedAttribute, setSelectedAttribute] = useState<string>("");
    const [isExporting, setIsExporting] = useState(false);
    const analyticsContentRef = React.useRef<HTMLDivElement>(null);

    const {eventsData, isLoadingEvents} = useEvents(0, 100);
    const events = eventsData?.content ?? [];
    const {definitions: attributes} = useAttributes();
    const {breakdown, stats, isLoadingStats, isLoadingBreakdown} = useAnalytics(selectedEventId, selectedAttribute);

    const handleExport = async () => {
        if (!analyticsContentRef.current || !selectedEventId) {
            toast.error("Please select an event.");
            return;
        }
        setIsExporting(true);
        toast.info("Preparing PDF...");
        try {
            const {exportToPdf} = await import("@/lib/pdf-exporter");
            await exportToPdf(analyticsContentRef.current, `analytics-report`);
        } catch {
            toast.error("Export Failed");
        } finally {
            setIsExporting(false);
        }
    };

    const formatTime = (isoStr: string | null) => {
        if (!isoStr) return "--:--";
        return format(new Date(isoStr), "h:mm a");
    };

    return (
        <SidebarProvider style={{"--sidebar-width": "calc(var(--spacing) * 72)", "--header-height": "calc(var(--spacing) * 12)"} as React.CSSProperties}>
            <AppSidebar variant="inset"/>
            <SidebarInset>
                <SiteHeader title="Analytics"/>
                <main className="flex-1 p-4 lg:p-6">
                    <div className="w-full max-w-6xl mx-auto space-y-6">
                        {/* Controls */}
                        <div className="flex flex-col gap-4 sm:flex-row justify-between items-center">
                            <div className="flex gap-4 w-full sm:w-auto">
                                {isLoadingEvents ? <Skeleton className="h-9 w-64"/> : (
                                    <Select value={selectedEventId} onValueChange={setSelectedEventId}>
                                        <SelectTrigger className="w-full sm:w-64"><SelectValue placeholder="Select Event"/></SelectTrigger>
                                        <SelectContent>
                                            {events.map(e => <SelectItem key={e.id} value={String(e.id)}>{e.name}</SelectItem>)}
                                        </SelectContent>
                                    </Select>
                                )}
                            </div>
                            <Button variant="outline" onClick={handleExport} disabled={isExporting || !selectedEventId}>
                                <IconFileDownload className="mr-2 h-4 w-4"/> Export PDF
                            </Button>
                        </div>

                        <div ref={analyticsContentRef} className="space-y-6 bg-background p-1">
                            {/* 1. Executive Summary Cards */}
                            {selectedEventId && (
                                <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                                    <Card>
                                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                            <CardTitle className="text-sm font-medium">Total Attendance</CardTitle>
                                            <IconUsers className="h-4 w-4 text-muted-foreground"/>
                                        </CardHeader>
                                        <CardContent>
                                            {isLoadingStats ? <Skeleton className="h-8 w-20"/> : (
                                                <>
                                                    <div className="text-2xl font-bold">{stats?.totalScans}</div>
                                                    <p className="text-xs text-muted-foreground">
                                                        {stats?.attendanceRate.toFixed(1)}% of {stats?.totalRoster} registered
                                                    </p>
                                                </>
                                            )}
                                        </CardContent>
                                    </Card>
                                    <Card>
                                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                            <CardTitle className="text-sm font-medium">Missing</CardTitle>
                                            <IconUsers className="h-4 w-4 text-muted-foreground"/>
                                        </CardHeader>
                                        <CardContent>
                                            {isLoadingStats ? <Skeleton className="h-8 w-20"/> : (
                                                <>
                                                    <div className="text-2xl font-bold text-red-600">
                                                        {Math.max(0, (stats?.totalRoster || 0) - (stats?.totalScans || 0))}
                                                    </div>
                                                    <p className="text-xs text-muted-foreground">Absent attendees</p>
                                                </>
                                            )}
                                        </CardContent>
                                    </Card>
                                    <Card>
                                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                            <CardTitle className="text-sm font-medium">Ingress Start</CardTitle>
                                            <IconClock className="h-4 w-4 text-muted-foreground"/>
                                        </CardHeader>
                                        <CardContent>
                                            {isLoadingStats ? <Skeleton className="h-8 w-20"/> : (
                                                <div className="text-2xl font-bold">{formatTime(stats?.firstScan || null)}</div>
                                            )}
                                        </CardContent>
                                    </Card>
                                    <Card>
                                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                            <CardTitle className="text-sm font-medium">Last Entry</CardTitle>
                                            <IconClock className="h-4 w-4 text-muted-foreground"/>
                                        </CardHeader>
                                        <CardContent>
                                            {isLoadingStats ? <Skeleton className="h-8 w-20"/> : (
                                                <div className="text-2xl font-bold">{formatTime(stats?.lastScan || null)}</div>
                                            )}
                                        </CardContent>
                                    </Card>
                                </div>
                            )}

                            {/* 2. Activity & Scanner Performance */}
                            {selectedEventId && (
                                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                                    <Card>
                                        <CardHeader>
                                            <CardTitle>Session Activity</CardTitle>
                                            <CardDescription>Which activities had the highest turnout?</CardDescription>
                                        </CardHeader>
                                        <CardContent>
                                            {isLoadingStats ? <Skeleton className="h-[300px]"/> : (
                                                <ChartContainer config={{count: {label: "Scans", color: "hsl(var(--primary))"}}} className="h-[300px] w-full">
                                                    <BarChart data={stats?.sessionStats || []} layout="vertical" margin={{left: 0}}>
                                                        <CartesianGrid horizontal={false}/>
                                                        <YAxis dataKey="label" type="category" width={120} tickLine={false} axisLine={false} style={{fontSize: '12px'}}/>
                                                        <XAxis type="number" hide/>
                                                        <ChartTooltip content={<ChartTooltipContent/>}/>
                                                        <Bar dataKey="count" fill="var(--color-count)" radius={4} barSize={32}/>
                                                    </BarChart>
                                                </ChartContainer>
                                            )}
                                        </CardContent>
                                    </Card>

                                    <Card>
                                        <CardHeader>
                                            <CardTitle>Top Scanners</CardTitle>
                                            <CardDescription>Volume processed by device/user.</CardDescription>
                                        </CardHeader>
                                        <CardContent>
                                            {isLoadingStats ? <Skeleton className="h-[300px]"/> : (
                                                <div className="space-y-4">
                                                    {(stats?.scannerStats || []).map((s, i) => (
                                                        <div key={i} className="flex items-center">
                                                            <div className="w-9 h-9 rounded-full bg-muted flex items-center justify-center mr-4">
                                                                {i === 0 ? <IconTrophy className="h-5 w-5 text-yellow-500"/> : <IconScan className="h-5 w-5 text-muted-foreground"/>}
                                                            </div>
                                                            <div className="flex-1 space-y-1">
                                                                <p className="text-sm font-medium leading-none">{s.label}</p>
                                                                <div className="h-2 w-full bg-secondary rounded-full overflow-hidden">
                                                                    <div className="h-full bg-primary" style={{width: `${(s.count / (stats?.totalScans || 1)) * 100}%`}}/>
                                                                </div>
                                                            </div>
                                                            <div className="font-bold ml-4">{s.count}</div>
                                                        </div>
                                                    ))}
                                                </div>
                                            )}
                                        </CardContent>
                                    </Card>
                                </div>
                            )}

                            {/* 3. Demographics Breakdown */}
                            {selectedEventId && (
                                <Card>
                                    <CardHeader>
                                        <div className="flex items-center justify-between">
                                            <div>
                                                <CardTitle>Demographics Breakdown</CardTitle>
                                                <CardDescription>Analyze attendance by attribute.</CardDescription>
                                            </div>
                                            <Select value={selectedAttribute} onValueChange={setSelectedAttribute}>
                                                <SelectTrigger className="w-48"><SelectValue placeholder="Select Attribute"/></SelectTrigger>
                                                <SelectContent>
                                                    {attributes.map(attr => <SelectItem key={attr.id} value={attr.name}>{attr.name}</SelectItem>)}
                                                </SelectContent>
                                            </Select>
                                        </div>
                                    </CardHeader>
                                    <CardContent>
                                        {!selectedAttribute ? (
                                            <div className="h-40 flex items-center justify-center text-muted-foreground border-2 border-dashed rounded-lg">
                                                Select an attribute above to view breakdown
                                            </div>
                                        ) : isLoadingBreakdown ? <Skeleton className="h-[300px]"/> : (
                                            <ChartContainer config={{count: {label: "Attendees", color: "hsl(var(--chart-2))"}}} className="h-[300px] w-full">
                                                <BarChart data={breakdown}>
                                                    <CartesianGrid vertical={false}/>
                                                    <XAxis dataKey="value" tickLine={false} axisLine={false} />
                                                    <ChartTooltip content={<ChartTooltipContent/>}/>
                                                    <Bar dataKey="count" fill="var(--color-count)" radius={4}/>
                                                </BarChart>
                                            </ChartContainer>
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
