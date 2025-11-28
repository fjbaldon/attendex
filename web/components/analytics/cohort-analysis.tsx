"use client";

import React, {useEffect, useState} from "react";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Button} from "@/components/ui/button";
import {IconAlertTriangle, IconChartPie, IconFilter, IconPlus, IconX} from "@tabler/icons-react";
import {Badge} from "@/components/ui/badge";
import {useAttributes} from "@/hooks/use-attributes";
import {useMutation} from "@tanstack/react-query";
import api from "@/lib/api";
import {ApiErrorResponse, CohortStatsDto, CohortStatsRequest} from "@/types";
import {Cell, Pie, PieChart, ResponsiveContainer, Tooltip} from "recharts";
import {Skeleton} from "@/components/ui/skeleton";
import {toast} from "sonner";
import {AxiosError} from "axios";

interface CohortAnalysisProps {
    eventId: string;
    sessionId?: number | null;
    filters: Record<string, string>;
    onFiltersChange: (filters: Record<string, string>) => void;
}

export function CohortAnalysis({eventId, sessionId, filters, onFiltersChange}: CohortAnalysisProps) {
    const {definitions: attributes} = useAttributes();

    // Local state for the dropdown inputs
    const [selectedAttr, setSelectedAttr] = useState<string>("");
    const [selectedValue, setSelectedValue] = useState<string>("");

    const mutation = useMutation<CohortStatsDto, AxiosError<ApiErrorResponse>, CohortStatsRequest>({
        mutationFn: async (req) => {
            if (!eventId) throw new Error("Event ID is missing");
            const res = await api.post(`/api/v1/insights/events/${eventId}/cohort`, req);
            return res.data;
        },
        onError: (error) => {
            console.error("Cohort analysis error:", error);
            const message = error.response?.data?.message || error.message || "Server error";
            toast.error("Failed to analyze cohort", { description: message });
        }
    });

    const { mutate, data, isPending, isError } = mutation;

    // Fetch on mount or when dependencies change
    useEffect(() => {
        mutate({
            filters: filters,
            sessionId: sessionId || null
        });
    }, [filters, sessionId, eventId, mutate]);

    const handleAddFilter = () => {
        if (selectedAttr && selectedValue) {
            const newFilters = {...filters, [selectedAttr]: selectedValue};
            onFiltersChange(newFilters);
            setSelectedAttr("");
            setSelectedValue("");
        }
    };

    const handleRemoveFilter = (key: string) => {
        const newFilters = {...filters};
        delete newFilters[key];
        onFiltersChange(newFilters);
    };

    const currentAttributeDef = attributes.find(a => a.name === selectedAttr);

    const chartData = data ? [
        {name: 'Present', value: data.presentCount, color: 'hsl(var(--primary))'},
        {name: 'Absent', value: data.absentCount, color: 'hsl(var(--muted))'}
    ] : [];

    const rate = data ? data.attendanceRate.toFixed(1) : "0.0";

    return (
        <div className="h-full flex flex-col bg-card">
            {/* Header Area */}
            <div className="p-6 pb-3">
                <h3 className="font-semibold leading-none tracking-tight flex items-center gap-2 text-base">
                    <IconChartPie className="w-4 h-4 text-muted-foreground"/>
                    Cohort Segments
                </h3>
                <p className="text-sm text-muted-foreground mt-1.5">
                    Filter to analyze specific groups.
                </p>
            </div>

            <div className="flex-1 flex flex-col gap-6 px-6 pb-6 min-h-0 overflow-y-auto">
                {/* 1. Filter Controls */}
                <div className="space-y-3 shrink-0">
                    <div className="flex gap-2">
                        <Select value={selectedAttr} onValueChange={setSelectedAttr}>
                            <SelectTrigger className="h-8 text-xs w-[130px]">
                                <SelectValue placeholder="Attribute"/>
                            </SelectTrigger>
                            <SelectContent>
                                {attributes.map(a => (
                                    <SelectItem key={a.id} value={a.name}>{a.name}</SelectItem>
                                ))}
                            </SelectContent>
                        </Select>

                        <Select value={selectedValue} onValueChange={setSelectedValue} disabled={!selectedAttr}>
                            <SelectTrigger className="h-8 text-xs flex-1">
                                <SelectValue placeholder="Value"/>
                            </SelectTrigger>
                            <SelectContent>
                                {currentAttributeDef?.options?.map(opt => (
                                    <SelectItem key={opt} value={opt}>{opt}</SelectItem>
                                ))}
                            </SelectContent>
                        </Select>

                        <Button size="sm" variant="secondary" className="h-8 px-2" onClick={handleAddFilter} disabled={!selectedAttr || !selectedValue || isPending}>
                            <IconPlus className="w-4 h-4"/>
                        </Button>
                    </div>

                    {/* Active Filters / Tags */}
                    <div className="flex flex-wrap gap-2 min-h-[24px]">
                        {Object.entries(filters).map(([key, val]) => (
                            <Badge key={key} variant="outline" className="h-6 gap-1 pl-2 pr-1 bg-background">
                                <span className="text-muted-foreground">{key}:</span> {val}
                                <button onClick={() => handleRemoveFilter(key)} className="ml-1 hover:bg-destructive/10 rounded-full p-0.5 transition-colors">
                                    <IconX className="w-3 h-3"/>
                                </button>
                            </Badge>
                        ))}
                        {Object.keys(filters).length === 0 && (
                            <span className="text-xs text-muted-foreground italic py-1">
                                Showing all {sessionId ? "session" : "event"} attendees.
                            </span>
                        )}
                    </div>
                </div>

                {/* 2. Chart Area */}
                <div className="flex-1 flex flex-col items-center justify-center min-h-[200px] border rounded-lg bg-muted/5 relative overflow-hidden">
                    {isPending && (
                        <div className="absolute inset-0 bg-background/50 flex items-center justify-center z-10 backdrop-blur-sm">
                            <Skeleton className="w-32 h-32 rounded-full"/>
                        </div>
                    )}

                    {isError ? (
                        <div className="text-center text-destructive p-6 animate-in fade-in">
                            <IconAlertTriangle className="w-8 h-8 mx-auto mb-2 opacity-50"/>
                            <p className="text-sm font-medium">Failed to load data</p>
                            <Button variant="link" size="sm" onClick={() => mutate({filters, sessionId: sessionId || null})}>
                                Try Again
                            </Button>
                        </div>
                    ) : !data ? (
                        <div className="text-center text-muted-foreground p-6">
                            <IconFilter className="w-8 h-8 mx-auto mb-2 opacity-20"/>
                            <p className="text-sm">Define a cohort above to see stats.</p>
                        </div>
                    ) : (
                        <div className="w-full h-full flex flex-row items-center justify-around p-4 animate-in zoom-in-95">
                            <div className="w-[160px] h-[160px] relative">
                                <ResponsiveContainer width="100%" height="100%">
                                    <PieChart>
                                        <Pie
                                            data={chartData}
                                            cx="50%"
                                            cy="50%"
                                            innerRadius={50}
                                            outerRadius={70}
                                            paddingAngle={2}
                                            dataKey="value"
                                            stroke="none"
                                        >
                                            {chartData.map((entry, index) => (
                                                <Cell key={`cell-${index}`} fill={entry.color}/>
                                            ))}
                                        </Pie>
                                        <Tooltip
                                            content={({ active, payload }) => {
                                                if (active && payload && payload.length) {
                                                    return (
                                                        <div className="rounded-lg border bg-background p-2 shadow-sm text-xs">
                                                            <span className="font-bold">{payload[0].name}:</span> {payload[0].value}
                                                        </div>
                                                    );
                                                }
                                                return null;
                                            }}
                                        />
                                    </PieChart>
                                </ResponsiveContainer>
                                <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                                    <span className="text-xl font-bold text-foreground">{rate}%</span>
                                </div>
                            </div>

                            <div className="flex flex-col gap-3 text-right min-w-[80px]">
                                <div>
                                    <div className="text-2xl font-bold">{rate}%</div>
                                    <div className="text-xs text-muted-foreground uppercase tracking-wider">Turnout</div>
                                </div>
                                <div className="text-sm space-y-1">
                                    <div className="flex items-center justify-end gap-2">
                                        <span className="font-semibold text-primary">{data.presentCount}</span>
                                        <div className="w-2 h-2 rounded-full bg-primary"/>
                                    </div>
                                    <div className="flex items-center justify-end gap-2">
                                        <span className="text-muted-foreground">{data.absentCount}</span>
                                        <div className="w-2 h-2 rounded-full bg-muted"/>
                                    </div>
                                    <div className="border-t pt-1 mt-1 text-xs text-muted-foreground">
                                        Total: {data.totalCohortSize}
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
