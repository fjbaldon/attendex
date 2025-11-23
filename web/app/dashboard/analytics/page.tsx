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
import {Bar, CartesianGrid, Cell, XAxis, YAxis} from "recharts";
import {ChartContainer, ChartTooltip, ChartTooltipContent} from "@/components/ui/chart";
import {Skeleton} from "@/components/ui/skeleton";
import {Icon, IconActivity, IconClock, IconUsers} from "@tabler/icons-react";
import {format} from "date-fns";
import {useAttributes} from "@/hooks/use-attributes";

const BarChart = dynamic(() => import("recharts").then(mod => mod.BarChart), {ssr: false});

export default function AnalyticsPage() {
    const [selectedEventId, setSelectedEventId] = useState<string>("");
    const [selectedAttribute, setSelectedAttribute] = useState<string>("");

    const {eventsData, isLoadingEvents} = useEvents(0, 100);
    const events = eventsData?.content ?? [];
    const {definitions: attributes, isLoading: isLoadingAttributes} = useAttributes();
    const {breakdown, stats, isLoadingStats, isLoadingBreakdown} = useAnalytics(selectedEventId, selectedAttribute);

    React.useEffect(() => {
        if (!selectedAttribute && attributes.length > 0) {
            setSelectedAttribute(attributes[0].name);
        }
    }, [attributes, selectedAttribute]);

    const formatTime = (isoStr: string | null | undefined) => {
        if (!isoStr) return "--:--";
        try {
            return format(new Date(isoStr), "h:mm a");
        } catch {
            return "--:--";
        }
    };

    const CHART_COLORS = ["#2563eb", "#16a34a", "#d97706", "#dc2626", "#7c3aed"];

    return (
        <SidebarProvider style={{"--sidebar-width": "calc(var(--spacing) * 72)", "--header-height": "calc(var(--spacing) * 12)"} as React.CSSProperties}>
            <AppSidebar variant="inset"/>
            <SidebarInset>
                <SiteHeader title="Analytics"/>
                <main className="flex-1 p-4 lg:p-6 overflow-y-auto">
                    <div className="w-full max-w-6xl mx-auto space-y-6">

                        {/* Header Control Bar */}
                        <div className="flex flex-col sm:flex-row gap-4 justify-between items-start sm:items-center bg-background/50 backdrop-blur-sm sticky top-0 z-10 pb-4 border-b">
                            <div className="w-full sm:w-72">
                                {isLoadingEvents ? <Skeleton className="h-10 w-full"/> : (
                                    <Select value={selectedEventId} onValueChange={setSelectedEventId}>
                                        <SelectTrigger className="h-10 w-full shadow-sm">
                                            <SelectValue placeholder="Select Event to Analyze"/>
                                        </SelectTrigger>
                                        <SelectContent>
                                            {events.map(e => <SelectItem key={e.id} value={String(e.id)}>{e.name}</SelectItem>)}
                                        </SelectContent>
                                    </Select>
                                )}
                            </div>
                            {/* Removed Export Button */}
                        </div>

                        <div className="space-y-6">
                            {!selectedEventId ? (
                                <div className="h-[60vh] flex flex-col items-center justify-center text-muted-foreground border-2 border-dashed rounded-xl bg-muted/5">
                                    <IconActivity className="h-16 w-16 mb-4 opacity-20"/>
                                    <p className="text-lg font-medium">No Event Selected</p>
                                    <p className="text-sm">Choose an event from the dropdown above to view analytics.</p>
                                </div>
                            ) : (
                                <>
                                    {/* 1. KPI Cards */}
                                    <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                                        <KpiCard title="Total Scans" icon={IconUsers} value={stats?.totalScans} subtext={`${(stats?.attendanceRate ?? 0).toFixed(0)}% Attendance`} loading={isLoadingStats} />
                                        <KpiCard title="Absent" icon={IconUsers} value={Math.max(0, (stats?.totalRoster || 0) - (stats?.totalScans || 0))} subtext="Not Scanned" loading={isLoadingStats} color="text-red-600" />
                                        <KpiCard title="First Scan" icon={IconClock} value={formatTime(stats?.firstScan)} subtext="Ingress Start" loading={isLoadingStats} />
                                        <KpiCard title="Last Scan" icon={IconClock} value={formatTime(stats?.lastScan)} subtext="Latest Entry" loading={isLoadingStats} />
                                    </div>

                                    {/* 2. Charts Row */}
                                    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                                        {/* Session Performance (Horizontal Bar) */}
                                        <Card className="lg:col-span-2 shadow-sm">
                                            <CardHeader>
                                                <CardTitle>Session Engagement</CardTitle>
                                                <CardDescription>Attendance volume per session</CardDescription>
                                            </CardHeader>
                                            <CardContent className="pl-0">
                                                {isLoadingStats ? <Skeleton className="h-[300px] m-4"/> : (
                                                    <ChartContainer config={{count: {label: "Attendees", color: "hsl(var(--primary))"}}} className="h-[300px] w-full">
                                                        <BarChart data={stats?.sessionStats || []} layout="vertical" margin={{left: 20, right: 20}}>
                                                            <CartesianGrid horizontal={false} strokeDasharray="3 3" strokeOpacity={0.5}/>
                                                            <YAxis dataKey="label" type="category" width={140} tickLine={false} axisLine={false} style={{fontSize: '12px', fontWeight: 500}}/>
                                                            <XAxis type="number" hide/>
                                                            <ChartTooltip cursor={{fill: 'transparent'}} content={<ChartTooltipContent/>}/>
                                                            <Bar dataKey="count" radius={[0, 4, 4, 0]} barSize={24}>
                                                                {(stats?.sessionStats || []).map((_, index) => (
                                                                    <Cell key={`cell-${index}`} fill={CHART_COLORS[index % CHART_COLORS.length]} />
                                                                ))}
                                                            </Bar>
                                                        </BarChart>
                                                    </ChartContainer>
                                                )}
                                            </CardContent>
                                        </Card>

                                        {/* Scanner Leaderboard */}
                                        <Card className="shadow-sm flex flex-col">
                                            <CardHeader>
                                                <CardTitle>Top Scanners</CardTitle>
                                                <CardDescription>Device performance</CardDescription>
                                            </CardHeader>
                                            <CardContent className="flex-1 overflow-hidden">
                                                {isLoadingStats ? <div className="space-y-4"><Skeleton className="h-12"/><Skeleton className="h-12"/></div> : (
                                                    <div className="space-y-5">
                                                        {(stats?.scannerStats || []).slice(0, 5).map((s, i) => (
                                                            <div key={i} className="group">
                                                                <div className="flex justify-between text-sm mb-1">
                                                                    <span className="font-medium truncate pr-2" title={s.label}>{s.label.split('@')[0]}</span>
                                                                    <span className="font-bold text-muted-foreground">{s.count}</span>
                                                                </div>
                                                                <div className="h-2 w-full bg-secondary/50 rounded-full overflow-hidden">
                                                                    <div className="h-full bg-primary transition-all duration-500" style={{width: `${(s.count / (stats?.totalScans || 1)) * 100}%`}}/>
                                                                </div>
                                                            </div>
                                                        ))}
                                                        {(!stats?.scannerStats || stats.scannerStats.length === 0) && (
                                                            <div className="text-sm text-muted-foreground text-center py-8">No data available.</div>
                                                        )}
                                                    </div>
                                                )}
                                            </CardContent>
                                        </Card>
                                    </div>

                                    {/* 3. Demographics */}
                                    <Card className="shadow-sm">
                                        <CardHeader className="flex flex-row items-center justify-between pb-2">
                                            <div className="space-y-1">
                                                <CardTitle>Demographics</CardTitle>
                                                <CardDescription>Breakdown by attribute</CardDescription>
                                            </div>
                                            {isLoadingAttributes ? <Skeleton className="h-8 w-32"/> : (
                                                <Select value={selectedAttribute} onValueChange={setSelectedAttribute} disabled={attributes.length === 0}>
                                                    <SelectTrigger className="h-8 w-[180px]">
                                                        <SelectValue placeholder="Attribute"/>
                                                    </SelectTrigger>
                                                    <SelectContent>
                                                        {attributes.map(attr => <SelectItem key={attr.id} value={attr.name}>{attr.name}</SelectItem>)}
                                                    </SelectContent>
                                                </Select>
                                            )}
                                        </CardHeader>
                                        <CardContent>
                                            {!selectedAttribute ? (
                                                <div className="h-[200px] flex items-center justify-center text-muted-foreground border-2 border-dashed rounded-lg bg-muted/5">
                                                    Select an attribute to view breakdown
                                                </div>
                                            ) : isLoadingBreakdown ? <Skeleton className="h-[250px]"/> : (
                                                <ChartContainer config={{count: {label: "Attendees", color: "hsl(var(--chart-1))"}}} className="h-[250px] w-full">
                                                    <BarChart data={breakdown} margin={{top: 10}}>
                                                        <CartesianGrid vertical={false} strokeDasharray="3 3"/>
                                                        <XAxis dataKey="value" tickLine={false} axisLine={false} tickMargin={10} />
                                                        <ChartTooltip content={<ChartTooltipContent/>}/>
                                                        <Bar dataKey="count" fill="var(--color-count)" radius={[4, 4, 0, 0]} barSize={40} />
                                                    </BarChart>
                                                </ChartContainer>
                                            )}
                                        </CardContent>
                                    </Card>
                                </>
                            )}
                        </div>
                    </div>
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}

interface KpiCardProps {
    title: string;
    icon: Icon;
    value: string | number | undefined | null;
    subtext: string;
    loading: boolean;
    color?: string;
}

function KpiCard({title, icon: Icon, value, subtext, loading, color}: KpiCardProps) {
    return (
        <Card className="shadow-sm">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">{title}</CardTitle>
                <Icon className="h-4 w-4 text-muted-foreground opacity-70"/>
            </CardHeader>
            <CardContent>
                {loading ? <Skeleton className="h-8 w-20"/> : (
                    <>
                        <div className={`text-2xl font-bold ${color || ''}`}>{value ?? 0}</div>
                        <p className="text-xs text-muted-foreground mt-1">{subtext}</p>
                    </>
                )}
            </CardContent>
        </Card>
    );
}
