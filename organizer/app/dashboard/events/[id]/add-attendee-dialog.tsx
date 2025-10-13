"use client";

import * as React from "react";
import {useState} from "react";
import {Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle,} from "@/components/ui/dialog";
import {useAttendees} from "@/hooks/use-attendees";
import {useEventDetails} from "@/hooks/use-event-details";
import {Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList} from "@/components/ui/command";
import {AttendeeResponse} from "@/types";
import {Button} from "@/components/ui/button";
import {useDebounce} from "@uidotdev/usehooks";

interface AddAttendeeDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    eventId: number;
    eventAttendees: AttendeeResponse[];
}

export function AddAttendeeDialog({open, onOpenChange, eventId, eventAttendees}: AddAttendeeDialogProps) {
    const [searchQuery, setSearchQuery] = useState("");
    const debouncedSearchQuery = useDebounce(searchQuery, 300);

    const {attendees: allAttendees, isLoadingAttendees} = useAttendees(0, 100); // Fetch all attendees
    const {addAttendee, isAddingAttendee} = useEventDetails(eventId);

    const availableAttendees = allAttendees.filter(
        (attendee) => !eventAttendees.some(eventAttendee => eventAttendee.id === attendee.id)
    );

    const filteredAttendees = debouncedSearchQuery ? availableAttendees.filter(
        (attendee) =>
            attendee.firstName.toLowerCase().includes(debouncedSearchQuery.toLowerCase()) ||
            attendee.lastName.toLowerCase().includes(debouncedSearchQuery.toLowerCase()) ||
            attendee.uniqueIdentifier.toLowerCase().includes(debouncedSearchQuery.toLowerCase())
    ) : availableAttendees;

    const handleSelectAttendee = (attendeeId: number) => {
        addAttendee({eventId, attendeeId}, {
            onSuccess: () => {
                onOpenChange(false);
            }
        });
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-md">
                <DialogHeader>
                    <DialogTitle>Add Attendee to Event</DialogTitle>
                    <DialogDescription>
                        Search for an attendee to add to this event's roster.
                    </DialogDescription>
                </DialogHeader>
                <Command shouldFilter={false}>
                    <CommandInput
                        placeholder="Search by name or identifier..."
                        value={searchQuery}
                        onValueChange={setSearchQuery}
                    />
                    <CommandList>
                        {isLoadingAttendees && <CommandEmpty>Loading attendees...</CommandEmpty>}
                        <CommandEmpty>No attendees found.</CommandEmpty>
                        <CommandGroup>
                            {filteredAttendees.map((attendee) => (
                                <CommandItem
                                    key={attendee.id}
                                    value={`${attendee.firstName} ${attendee.lastName} ${attendee.uniqueIdentifier}`}
                                    onSelect={() => handleSelectAttendee(attendee.id)}
                                >
                                    <div className="flex flex-col">
                                        <span className="font-medium">{attendee.firstName} {attendee.lastName}</span>
                                        <span
                                            className="text-xs text-muted-foreground">{attendee.uniqueIdentifier}</span>
                                    </div>
                                </CommandItem>
                            ))}
                        </CommandGroup>
                    </CommandList>
                </Command>
                <Button variant="outline" onClick={() => onOpenChange(false)} className="mt-4">
                    Cancel
                </Button>
            </DialogContent>
        </Dialog>
    );
}
