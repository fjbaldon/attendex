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
import {Skeleton} from "@/components/ui/skeleton";

interface BulkRecoverDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    count: number;
    onConfirm: (targetEventId: number) => void;
    isLoading: boolean;
}

export function BulkRecoverDialog({open, onOpenChange, count, onConfirm, isLoading}: BulkRecoverDialogProps) {
    const {eventsData, isLoadingEvents} = useEvents(0, 50);
    const [selectedEventId, setSelectedEventId] = React.useState<string>("");

    const handleConfirm = () => {
        if (selectedEventId) {
            onConfirm(Number(selectedEventId));
        }
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-[425px]">
                <DialogHeader>
                    <DialogTitle>Bulk Recover Entries</DialogTitle>
                    <DialogDescription>
                        You are about to recover <span className="font-medium text-foreground">{count}</span> entries.
                        Please select the event they belong to.
                    </DialogDescription>
                </DialogHeader>

                <div className="grid gap-4 py-4">
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
                        {isLoading ? "Recovering..." : `Recover ${count} Entries`}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
