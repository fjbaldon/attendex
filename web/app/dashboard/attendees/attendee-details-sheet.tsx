"use client";

import {Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle} from "@/components/ui/sheet";
import {useAttendeeHistory} from "@/hooks/use-attendee-history";
import {Avatar, AvatarFallback} from "@/components/ui/avatar";
import {Skeleton} from "@/components/ui/skeleton";
import {Badge} from "@/components/ui/badge";
import {IconAlertCircle, IconCalendar, IconCheck, IconClock, IconTrophy, IconUser} from "@tabler/icons-react";
import {format} from "date-fns";
import {Separator} from "@/components/ui/separator";
import {Accordion, AccordionContent, AccordionItem, AccordionTrigger} from "@/components/ui/accordion";
import {ScrollArea} from "@/components/ui/scroll-area";
import {cn} from "@/lib/utils";
import {SessionHistoryItem} from "@/types";
import {Card} from "@/components/ui/card";

interface AttendeeDetailsSheetProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    attendeeId: number | null;
}

export function AttendeeDetailsSheet({open, onOpenChange, attendeeId}: AttendeeDetailsSheetProps) {
    const {history, isLoading} = useAttendeeHistory(open ? attendeeId : null);

    if (!attendeeId) return null;

    // --- Helpers ---

    const formatTime = (iso: string) => format(new Date(iso), "h:mm a");

    // 1. Detailed Session Status Styling
    const getSessionStatusConfig = (status: string) => {
        switch (status) {
            case "PRESENT":
            case "PUNCTUAL":
                return { label: "On Time", className: "bg-green-100 text-green-700 border-green-200 dark:bg-green-500/20 dark:text-green-400 dark:border-green-900" };
            case "LATE":
                return { label: "Late", className: "bg-amber-100 text-amber-700 border-amber-200 dark:bg-amber-500/20 dark:text-amber-400 dark:border-amber-900" };
            case "EARLY":
                return { label: "Early", className: "bg-blue-100 text-blue-700 border-blue-200 dark:bg-blue-500/20 dark:text-blue-400 dark:border-blue-900" };
            case "ABSENT":
                return { label: "Absent", className: "bg-red-100 text-red-700 border-red-200 dark:bg-red-500/20 dark:text-red-400 dark:border-red-900" };
            case "PENDING":
            default:
                return { label: "Pending", className: "bg-muted text-muted-foreground border-border" };
        }
    };

    // 2. Event Level Punctuality Logic
    const getEventSummaryStatus = (sessions: SessionHistoryItem[], total: number) => {
        if (total === 0) return null;

        const completed = sessions.filter(s => s.status !== 'ABSENT' && s.status !== 'PENDING');
        const isComplete = completed.length === total;
        const hasLateness = completed.some(s => s.status === 'LATE');

        // Logic tree for the Event Card Badge
        if (isComplete) {
            if (hasLateness) return { label: "Late", color: "text-amber-600 bg-amber-50 border-amber-200" };
            return { label: "On Time", color: "text-green-600 bg-green-50 border-green-200" };
        }

        if (completed.length > 0) {
            return { label: "Partial", color: "text-muted-foreground bg-muted border-border" };
        }

        // If event is past and 0 completed
        if (sessions.some(s => s.status === 'ABSENT')) {
            return { label: "Missed", color: "text-red-600 bg-red-50 border-red-200" };
        }

        return { label: "Upcoming", color: "text-blue-600 bg-blue-50 border-blue-200" };
    };

    const renderContent = () => {
        if (isLoading || !history) {
            return (
                <div className="space-y-8 px-6 pt-6">
                    <div className="flex flex-col items-center gap-4">
                        <Skeleton className="h-24 w-24 rounded-full" />
                        <div className="space-y-2 text-center">
                            <Skeleton className="h-6 w-48 mx-auto" />
                            <Skeleton className="h-4 w-32 mx-auto" />
                        </div>
                    </div>
                    <div className="grid grid-cols-3 gap-4">
                        <Skeleton className="h-20 w-full" />
                        <Skeleton className="h-20 w-full" />
                        <Skeleton className="h-20 w-full" />
                    </div>
                    <div className="space-y-4">
                        <Skeleton className="h-24 w-full" />
                        <Skeleton className="h-24 w-full" />
                    </div>
                </div>
            );
        }

        const {profile} = history;
        const initials = `${profile.firstName[0]}${profile.lastName[0]}`;

        return (
            <div className="flex flex-col min-h-full">
                {/* 1. Profile Header */}
                <div className="flex flex-col items-center justify-center pt-8 pb-6 bg-muted/30 border-b">
                    <Avatar className="h-24 w-24 border-4 border-background shadow-sm mb-3">
                        <AvatarFallback className="text-3xl bg-primary/10 text-primary font-light tracking-widest">
                            {initials}
                        </AvatarFallback>
                    </Avatar>
                    <h2 className="text-xl font-bold tracking-tight text-center px-4">
                        {profile.firstName} {profile.lastName}
                    </h2>
                    <Badge variant="secondary" className="mt-2 font-mono text-xs tracking-wide bg-background border">
                        {profile.identity}
                    </Badge>
                </div>

                <div className="px-6 space-y-8 pt-6 pb-32">

                    {/* 2. Stats Row */}
                    <div className="grid grid-cols-3 divide-x border rounded-xl bg-card shadow-sm overflow-hidden">
                        <div className="flex flex-col items-center justify-center p-3 text-center hover:bg-muted/20 transition-colors">
                            <div className="text-2xl font-bold text-primary">
                                {history.attendanceRate.toFixed(0)}<span className="text-sm align-top opacity-50">%</span>
                            </div>
                            <div className="text-[10px] uppercase tracking-wider text-muted-foreground font-semibold mt-1 flex items-center gap-1">
                                <IconTrophy className="w-3 h-3" /> Rate
                            </div>
                        </div>
                        <div className="flex flex-col items-center justify-center p-3 text-center hover:bg-muted/20 transition-colors">
                            <div className="text-2xl font-bold text-foreground">
                                {history.totalAttended}
                            </div>
                            <div className="text-[10px] uppercase tracking-wider text-muted-foreground font-semibold mt-1 flex items-center gap-1">
                                <IconCheck className="w-3 h-3" /> Present
                            </div>
                        </div>
                        <div className="flex flex-col items-center justify-center p-3 text-center hover:bg-muted/20 transition-colors">
                            <div className="text-2xl font-bold text-red-600">
                                {history.totalAbsent}
                            </div>
                            <div className="text-[10px] uppercase tracking-wider text-muted-foreground font-semibold mt-1 flex items-center gap-1">
                                <IconAlertCircle className="w-3 h-3" /> Absent
                            </div>
                        </div>
                    </div>

                    {/* 3. Attributes */}
                    {profile.attributes && Object.keys(profile.attributes).length > 0 && (
                        <div className="space-y-3">
                            <h3 className="text-sm font-semibold text-muted-foreground flex items-center gap-2">
                                <IconUser className="w-4 h-4" /> Details
                            </h3>
                            <div className="grid grid-cols-2 gap-3">
                                {Object.entries(profile.attributes).map(([key, value]) => (
                                    <Card key={key} className="p-3 flex flex-col gap-1 shadow-sm border bg-card/50">
                                        <span className="text-[10px] text-muted-foreground uppercase tracking-wider font-semibold">{key}</span>
                                        <span className="font-medium text-sm truncate" title={String(value)}>{String(value)}</span>
                                    </Card>
                                ))}
                            </div>
                        </div>
                    )}

                    <Separator className="my-2" />

                    {/* 4. Event Timeline */}
                    <div className="space-y-4">
                        <h3 className="text-sm font-semibold text-muted-foreground flex items-center gap-2">
                            <IconCalendar className="w-4 h-4" /> History
                        </h3>

                        {history.history.length === 0 ? (
                            <div className="flex flex-col items-center justify-center py-12 text-muted-foreground bg-muted/10 rounded-lg border-2 border-dashed">
                                <IconCalendar className="w-10 h-10 mb-2 opacity-20" />
                                <p className="text-sm">No events found.</p>
                            </div>
                        ) : (
                            <Accordion type="single" collapsible className="w-full space-y-4">
                                {history.history.map((event, index) => {
                                    const isLast = index === history.history.length - 1;
                                    const summaryStatus = getEventSummaryStatus(event.sessions, event.totalSessions);

                                    return (
                                        <div key={event.eventId} className="relative pl-4">
                                            {!isLast && (
                                                <div className="absolute left-[23px] top-8 bottom-[-32px] w-px border-l-2 border-dashed border-muted-foreground/20 z-0" />
                                            )}

                                            <AccordionItem
                                                value={String(event.eventId)}
                                                className="border rounded-xl bg-card shadow-sm data-[state=open]:ring-1 data-[state=open]:ring-primary/20 transition-all relative z-10 overflow-hidden"
                                            >
                                                <AccordionTrigger className="px-4 py-3 hover:no-underline hover:bg-muted/30 group">
                                                    <div className="flex items-center gap-3 w-full text-left">

                                                        {/* Simple Dot Indicator for Completion */}
                                                        <div className={cn(
                                                            "w-2.5 h-2.5 rounded-full shrink-0 shadow-sm",
                                                            event.sessionsCompleted === event.totalSessions ? "bg-green-500" :
                                                                event.sessionsCompleted > 0 ? "bg-amber-500" : "bg-muted-foreground/30"
                                                        )} />

                                                        <div className="flex-1 min-w-0">
                                                            <div className="flex justify-between items-center">
                                                                <span className="font-semibold text-sm truncate mr-2">{event.eventName}</span>

                                                                {/* EVENT LEVEL PUNCTUALITY SUMMARY BADGE */}
                                                                {summaryStatus && (
                                                                    <Badge variant="outline" className={cn("text-[10px] h-5 px-1.5 font-medium border", summaryStatus.color)}>
                                                                        {summaryStatus.label}
                                                                    </Badge>
                                                                )}
                                                            </div>

                                                            <div className="flex items-center gap-2 mt-1">
                                                                <span className="text-xs text-muted-foreground font-mono">
                                                                    {format(new Date(event.eventDate), "MMM d")}
                                                                </span>
                                                                <span className="text-[10px] text-muted-foreground/60">•</span>
                                                                <span className="text-xs text-muted-foreground">
                                                                    {event.sessionsCompleted}/{event.totalSessions} Sessions
                                                                </span>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </AccordionTrigger>

                                                <AccordionContent className="px-0 pb-0 border-t bg-muted/5">
                                                    <div className="flex flex-col divide-y">
                                                        {event.sessions.length === 0 ? (
                                                            <div className="p-4 text-center text-xs text-muted-foreground italic">
                                                                No sessions configured.
                                                            </div>
                                                        ) : (
                                                            event.sessions.map((session) => {
                                                                const statusConfig = getSessionStatusConfig(session.status);
                                                                return (
                                                                    <div key={session.sessionId} className="flex items-center justify-between p-3 px-4 hover:bg-muted/40 transition-colors group/row">
                                                                        <div className="flex flex-col gap-0.5 min-w-0">
                                                                            <div className="flex items-center gap-2">
                                                                                <Badge variant="secondary" className="text-[9px] h-4 px-1 font-normal uppercase tracking-wider bg-background border text-muted-foreground">
                                                                                    {session.intent.substring(0, 3)}
                                                                                </Badge>
                                                                                <span className="font-medium text-sm truncate text-foreground">
                                                                                    {session.activityName}
                                                                                </span>
                                                                            </div>
                                                                            <div className="text-xs text-muted-foreground pl-1 flex items-center gap-1">
                                                                                <IconClock className="w-3 h-3 opacity-50" />
                                                                                Target: {formatTime(session.targetTime)}
                                                                            </div>
                                                                        </div>

                                                                        <div className="flex flex-col items-end gap-1 shrink-0">
                                                                            {/* SESSION SPECIFIC PUNCTUALITY BADGE */}
                                                                            <Badge variant="outline" className={cn("text-[10px] px-2 h-5 border font-medium", statusConfig.className)}>
                                                                                {statusConfig.label}
                                                                            </Badge>

                                                                            {session.scanTime ? (
                                                                                <span className="text-[10px] text-muted-foreground font-mono">
                                                                                    {formatTime(session.scanTime)}
                                                                                </span>
                                                                            ) : (
                                                                                <span className="text-[10px] text-muted-foreground/40 italic">--:--</span>
                                                                            )}
                                                                        </div>
                                                                    </div>
                                                                );
                                                            })
                                                        )}
                                                    </div>
                                                </AccordionContent>
                                            </AccordionItem>
                                        </div>
                                    );
                                })}
                            </Accordion>
                        )}
                    </div>
                </div>
            </div>
        );
    };

    return (
        <Sheet open={open} onOpenChange={onOpenChange}>
            <SheetContent className="w-full sm:max-w-md p-0 flex flex-col h-full border-l shadow-2xl" side="right">
                <SheetHeader className="px-6 py-4 border-b bg-background/80 backdrop-blur-sm z-10 shrink-0">
                    <SheetTitle>Attendee Profile</SheetTitle>
                    <SheetDescription>History and participation details.</SheetDescription>
                </SheetHeader>

                {/* MAIN SCROLL AREA */}
                <ScrollArea className="flex-1 h-full w-full">
                    {renderContent()}
                </ScrollArea>
            </SheetContent>
        </Sheet>
    );
}
