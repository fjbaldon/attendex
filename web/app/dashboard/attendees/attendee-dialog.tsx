"use client";

import * as React from "react";
import {z} from "zod";
import {attendeeSchema} from "@/lib/schemas";
import {useIsMobile} from "@/hooks/use-mobile";
import {AttendeeResponse, UpdateAttendeeRequest} from "@/types";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle
} from "@/components/ui/dialog";
import {
    Drawer,
    DrawerContent,
    DrawerDescription,
    DrawerFooter,
    DrawerHeader,
    DrawerTitle
} from "@/components/ui/drawer";
import {AttendeeForm} from "./attendee-form";
import {useAttendees} from "@/hooks/use-attendees";
import {Button} from "@/components/ui/button";

interface AttendeeDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    attendee?: AttendeeResponse | null;
}

export function AttendeeDialog({open, onOpenChange, attendee}: AttendeeDialogProps) {
    const isMobile = useIsMobile();
    const isEditing = !!attendee;
    const formId = "attendee-form";

    const {createAttendee, isCreatingAttendee, updateAttendee, isUpdatingAttendee} = useAttendees();
    const isLoading = isCreatingAttendee || isUpdatingAttendee;

    const title = isEditing ? "Edit Attendee" : "Add Attendee";
    const description = isEditing
        ? "Update the attendee's details below."
        : "Enter the details for the new attendee.";

    const handleSubmit = (values: z.infer<typeof attendeeSchema>) => {
        if (isEditing && attendee) {
            const updatePayload: UpdateAttendeeRequest = {
                firstName: values.firstName,
                lastName: values.lastName,
                attributes: values.attributes,
            };

            updateAttendee({id: attendee.id, data: updatePayload}, {
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
            key={attendee?.id || 'new'}
            attendee={attendee}
            onSubmit={handleSubmit}
            formId={formId}
        />
    );

    const footer = (
        <div className="flex gap-2 justify-end w-full">
            <Button variant="outline" onClick={() => onOpenChange(false)}>Cancel</Button>
            <Button type="submit" form={formId} disabled={isLoading}>
                {isLoading ? "Saving..." : isEditing ? "Save Changes" : "Create Attendee"}
            </Button>
        </div>
    );

    if (isMobile) {
        return (
            <Drawer open={open} onOpenChange={onOpenChange}>
                <DrawerContent className="max-h-[90vh] flex flex-col">
                    <DrawerHeader className="text-left">
                        <DrawerTitle>{title}</DrawerTitle>
                        <DrawerDescription>{description}</DrawerDescription>
                    </DrawerHeader>
                    <div className="flex-1 overflow-y-auto px-4 py-4">
                        {form}
                    </div>
                    <DrawerFooter className="pt-2 border-t bg-muted/10">
                        {footer}
                    </DrawerFooter>
                </DrawerContent>
            </Drawer>
        );
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-[480px] max-h-[85vh] flex flex-col p-0 gap-0">
                <DialogHeader className="p-6 pb-4 border-b">
                    <DialogTitle>{title}</DialogTitle>
                    <DialogDescription>{description}</DialogDescription>
                </DialogHeader>

                <div className="flex-1 overflow-y-auto p-6">
                    {form}
                </div>

                <DialogFooter className="p-6 pt-4 border-t bg-muted/5">
                    {footer}
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
