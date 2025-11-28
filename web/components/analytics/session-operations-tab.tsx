"use client";

import React, {useState} from "react";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {IconClock, IconUsers} from "@tabler/icons-react";
import {EventStats, Session} from "@/types";
import {CohortAnalysis} from "./cohort-analysis";
import {KpiCard} from "./kpi-card";
import {SessionAttendeeList} from "./session-attendee-list";

interface SessionOperationsTabProps {
    stats?: EventStats;
    sessions: Session[]; // We pass the list of sessions here
    eventId: string;
    isLoadingStats: boolean;
}

export function SessionOperationsTab({ stats, sessions, eventId, isLoadingStats }: SessionOperationsTabProps) {
    const [selectedSessionId, setSelectedSessionId] = useState<string>("");

    const [cohortFilters, setCohortFilters] = useState<Record<string, string>>({});

    // Auto-select first session
    React.useEffect(() => {
        if (!selectedSessionId && sessions.length > 0) {
            setSelectedSessionId(String(sessions[0].id));
        }
    }, [sessions, selectedSessionId]);

    const selectedSession = sessions.find(s => String(s.id) === selectedSessionId);

    // Find stats specific to this session from the aggregate object
    // Note: If your backend 'sessionStats' doesn't contain punctuality, we rely on CohortAnalysis for deep data.
    // Here we find the basic count.
    const sessionStatItem = stats?.sessionStats.find(s => s.label === selectedSession?.activityName);
    const sessionCount = sessionStatItem?.count ?? 0;

    // We can show a simple distribution graph if we had it, but for now let's focus on the Count + Cohort

    return (
        <div className="space-y-6 animate-in fade-in slide-in-from-bottom-2 duration-500">

            {/* 1. SESSION SELECTOR TOOLBAR */}
            <div className="flex items-center gap-4 bg-muted/40 p-3 rounded-lg border">
                <span className="text-sm font-medium text-muted-foreground">Select Activity:</span>
                <Select value={selectedSessionId} onValueChange={setSelectedSessionId}>
                    <SelectTrigger className="w-[300px] h-9 bg-background">
                        <SelectValue placeholder="Select a session..." />
                    </SelectTrigger>
                    <SelectContent>
                        {sessions.map(s => (
                            <SelectItem key={s.id} value={String(s.id)}>
                                {s.activityName} <span className="text-muted-foreground text-xs ml-2">({s.intent})</span>
                            </SelectItem>
                        ))}
                    </SelectContent>
                </Select>
            </div>

            {/* 2. SESSION SPECIFIC KPIs */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <KpiCard
                    title="Verified Headcount"
                    icon={IconUsers}
                    value={sessionCount}
                    subtext="Total scans for this session"
                    loading={isLoadingStats}
                    color="text-primary"
                />
                <KpiCard
                    title="Scheduled Time"
                    icon={IconClock}
                    value={selectedSession ? new Date(selectedSession.targetTime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) : "--:--"}
                    subtext={selectedSession?.intent || "N/A"}
                    loading={false}
                />
            </div>

            {/* 3. SESSION DRILL DOWN */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

                {/* LEFT: Filter & Chart */}
                <div className="h-[500px] border-r border-border/50">
                    <CohortAnalysis
                        eventId={eventId}
                        sessionId={selectedSessionId ? Number(selectedSessionId) : null}
                        filters={cohortFilters}
                        onFiltersChange={setCohortFilters} // Pass setter
                    />
                </div>

                {/* RIGHT: Result List */}
                <div className="h-[500px]">
                    <SessionAttendeeList
                        eventId={eventId}
                        sessionId={selectedSessionId ? Number(selectedSessionId) : null}
                        filters={cohortFilters} // Pass state
                    />
                </div>
            </div>
        </div>
    );
}
