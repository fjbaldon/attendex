"use client";

import * as React from "react";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import {Button} from "@/components/ui/button";
import {Label} from "@/components/ui/label";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {useEvents} from "@/hooks/use-events";
import {OrphanedEntry} from "@/types";
import {Skeleton} from "@/components/ui/skeleton";

interface RecoverDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    orphan: OrphanedEntry | null;
    onConfirm: (targetEventId: number) => void;
    isLoading: boolean;
}

export function RecoverDialog({open, onOpenChange, orphan, onConfirm, isLoading}: RecoverDialogProps) {
    // Fetch active events to list as targets
    const {eventsData, isLoadingEvents} = useEvents(0, 50);
    const [selectedEventId, setSelectedEventId] = React.useState<string>("");

    const handleConfirm = () => {
        if (selectedEventId) {
            onConfirm(Number(selectedEventId));
        }
    };

    // Auto-select if the list only has one event? Maybe too presumptuous.
    // Instead, if the orphan has an originalEventId that exists in the active list, pre-select it.
    React.useEffect(() => {
        if (open && orphan && eventsData?.content) {
            const match = eventsData.content.find(e => e.id === orphan.originalEventId);
            if (match) {
                setSelectedEventId(String(match.id));
            } else {
                setSelectedEventId("");
            }
        }
    }, [open, orphan, eventsData]);

    if (!orphan) return null;

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-[425px]">
                <DialogHeader>
                    <DialogTitle>Recover Entry</DialogTitle>
                    <DialogDescription>
                        Assign this scan to an active event. The system will recalculate punctuality based on the new event&#39;s schedule.
                    </DialogDescription>
                </DialogHeader>

                <div className="grid gap-4 py-4">
                    <div className="grid gap-2">
                        <Label>Original Event</Label>
                        <div className="text-sm text-muted-foreground border rounded-md p-2 bg-muted/50">
                            {orphan.originalEventName} <span className="text-xs opacity-50">(ID: {orphan.originalEventId})</span>
                        </div>
                    </div>

                    <div className="grid gap-2">
                        <Label>Target Event</Label>
                        {isLoadingEvents ? (
                            <Skeleton className="h-10 w-full" />
                        ) : (
                            <Select value={selectedEventId} onValueChange={setSelectedEventId}>
                                <SelectTrigger>
                                    <SelectValue placeholder="Select an event..." />
                                </SelectTrigger>
                                <SelectContent>
                                    {eventsData?.content.map(event => (
                                        <SelectItem key={event.id} value={String(event.id)}>
                                            {event.name}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        )}
                    </div>
                </div>

                <DialogFooter>
                    <Button variant="outline" onClick={() => onOpenChange(false)}>Cancel</Button>
                    <Button onClick={handleConfirm} disabled={isLoading || !selectedEventId}>
                        {isLoading ? "Recovering..." : "Recover"}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
