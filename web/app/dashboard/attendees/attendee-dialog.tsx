"use client";

import * as React from "react";
import {z} from "zod";
import {attendeeSchema} from "@/lib/schemas";
import {useIsMobile} from "@/hooks/use-mobile";
import {AttendeeResponse} from "@/types";
import {Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle} from "@/components/ui/dialog";
import {Drawer, DrawerContent, DrawerDescription, DrawerHeader, DrawerTitle} from "@/components/ui/drawer";
import {AttendeeForm} from "./attendee-form";
import {useAttendees} from "@/hooks/use-attendees";

interface AttendeeDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    attendee?: AttendeeResponse | null;
}

export function AttendeeDialog({open, onOpenChange, attendee}: AttendeeDialogProps) {
    const isMobile = useIsMobile();
    const isEditing = !!attendee;

    const {createAttendee, isCreatingAttendee, updateAttendee, isUpdatingAttendee} = useAttendees();

    const title = isEditing ? "Edit Attendee" : "Add a New Attendee";
    const description = isEditing
        ? "Make changes to this attendee's profile. Click save when you're done."
        : "Fill in the details below to create a new attendee.";

    const handleSubmit = (values: z.infer<typeof attendeeSchema>) => {
        if (isEditing && attendee) {
            updateAttendee({id: attendee.id, data: values}, {
                onSuccess: () => onOpenChange(false),
            });
        } else {
            createAttendee(values, {
                onSuccess: () => onOpenChange(false),
            });
        }
    };

    const form = (
        <AttendeeForm
            key={attendee?.id || 'new-attendee'}
            attendee={attendee}
            onSubmit={handleSubmit}
            isLoading={isCreatingAttendee || isUpdatingAttendee}
            onClose={() => onOpenChange(false)}
        />
    );

    if (isMobile) {
        return (
            <Drawer open={open} onOpenChange={onOpenChange}>
                <DrawerContent className="p-4">
                    <DrawerHeader className="text-left">
                        <DrawerTitle>{title}</DrawerTitle>
                        <DrawerDescription>{description}</DrawerDescription>
                    </DrawerHeader>
                    <div className="px-4">{form}</div>
                </DrawerContent>
            </Drawer>
        );
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-[480px]">
                <DialogHeader>
                    <DialogTitle>{title}</DialogTitle>
                    <DialogDescription>{description}</DialogDescription>
                </DialogHeader>
                {form}
            </DialogContent>
        </Dialog>
    );
}
