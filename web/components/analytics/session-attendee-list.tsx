"use client";

import React, {useState} from "react";
import {useQuery} from "@tanstack/react-query";
import api from "@/lib/api";
import {PaginatedResponse} from "@/types";
import {Badge} from "@/components/ui/badge";
import {ScrollArea} from "@/components/ui/scroll-area";
import {format} from "date-fns";
import {Skeleton} from "@/components/ui/skeleton";
import {Tabs, TabsList, TabsTrigger} from "@/components/ui/tabs";

interface CohortAttendee {
    id: number;
    identity: string;
    firstName: string;
    lastName: string;
    status: 'PRESENT' | 'ABSENT';
    scanTime?: string;
}

interface SessionAttendeeListProps {
    eventId: string;
    sessionId?: number | null;
    filters: Record<string, string>;
}

export function SessionAttendeeList({eventId, sessionId, filters}: SessionAttendeeListProps) {
    const [filterStatus, setFilterStatus] = useState<string>("ABSENT");

    const {data, isLoading} = useQuery<PaginatedResponse<CohortAttendee>>({
        queryKey: ['cohortList', eventId, sessionId, filters],
        queryFn: async () => {
            const res = await api.post(`/api/v1/insights/events/${eventId}/cohort/list`, {
                sessionId: sessionId || null,
                filters
            }, {
                params: { page: 0, size: 100, sort: 'lastName,asc' }
            });
            return res.data;
        },
        enabled: !!eventId
    });

    const allAttendees = data?.content || [];

    const visibleAttendees = allAttendees.filter(a => {
        if (filterStatus === "ALL") return true;
        return a.status === filterStatus;
    });

    return (
        <div className="h-full flex flex-col bg-card min-h-0">
            {/* Header Section with Tabs */}
            <div className="pb-2 border-b px-6 py-4 shrink-0 space-y-3">
                <div className="flex justify-between items-center">
                    <h3 className="font-semibold leading-none tracking-tight text-base">Attendee List</h3>
                    <Badge variant="secondary" className="font-mono">
                        {visibleAttendees.length}
                    </Badge>
                </div>

                <Tabs value={filterStatus} onValueChange={setFilterStatus} className="w-full">
                    <TabsList className="grid w-full grid-cols-3 h-8">
                        <TabsTrigger value="ABSENT" className="text-xs">Absent</TabsTrigger>
                        <TabsTrigger value="PRESENT" className="text-xs">Present</TabsTrigger>
                        <TabsTrigger value="ALL" className="text-xs">All</TabsTrigger>
                    </TabsList>
                </Tabs>
            </div>

            {/* Scrollable Content */}
            <div className="flex-1 min-h-0 overflow-hidden relative">
                <ScrollArea className="h-full w-full">
                    <div className="flex flex-col">
                        {isLoading ? (
                            <div className="p-4 space-y-3">
                                {[1,2,3].map(i => <Skeleton key={i} className="h-12 w-full" />)}
                            </div>
                        ) : visibleAttendees.length === 0 ? (
                            <div className="flex flex-col items-center justify-center h-40 text-center p-6 text-muted-foreground">
                                <p className="text-sm">
                                    {filterStatus === 'ABSENT'
                                        ? "Everyone in this group is present."
                                        : "No attendees found."}
                                </p>
                            </div>
                        ) : (
                            <div className="divide-y">
                                {visibleAttendees.map(a => (
                                    <div key={a.id} className="flex items-center justify-between p-3 px-6 hover:bg-muted/50 transition-colors">
                                        <div className="flex flex-col gap-0.5 overflow-hidden min-w-0 pr-4">
                                            <span className="font-medium text-sm truncate">{a.lastName}, {a.firstName}</span>
                                            <span className="text-xs text-muted-foreground font-mono truncate">{a.identity}</span>
                                        </div>
                                        <div className="text-right shrink-0">
                                            {a.status === 'PRESENT' ? (
                                                <div className="flex flex-col items-end gap-0.5">
                                                    <Badge className="bg-green-100 text-green-700 hover:bg-green-100 border-green-200 text-[10px] uppercase h-5 px-1.5 shadow-none dark:bg-green-900/30 dark:text-green-400 dark:border-green-800">
                                                        Present
                                                    </Badge>
                                                    {a.scanTime && (
                                                        <span className="text-[10px] text-muted-foreground font-mono">
                                                            {format(new Date(a.scanTime), "h:mm a")}
                                                        </span>
                                                    )}
                                                </div>
                                            ) : (
                                                <Badge variant="outline" className="text-red-600 border-red-200 bg-red-50 text-[10px] uppercase h-5 px-1.5 dark:bg-red-900/20 dark:text-red-400 dark:border-red-900">
                                                    Absent
                                                </Badge>
                                            )}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </ScrollArea>
            </div>
        </div>
    );
}
