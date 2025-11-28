"use client";

import * as React from "react";
import {z} from "zod";
import {eventSchema} from "@/lib/schemas";
import {useIsMobile} from "@/hooks/use-mobile";
import {EventResponse} from "@/types";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import {
    Drawer,
    DrawerContent,
    DrawerDescription,
    DrawerFooter,
    DrawerHeader,
    DrawerTitle,
} from "@/components/ui/drawer";
import {EventForm} from "./event-form";
import {Button} from "@/components/ui/button";

interface EventDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    event?: EventResponse | null;
    onSubmit: (values: z.infer<typeof eventSchema>) => void;
    isLoading: boolean;
}

export function EventDialog({open, onOpenChange, event, onSubmit, isLoading}: EventDialogProps) {
    const isMobile = useIsMobile();
    const isEditing = !!event;
    const formId = "event-form";

    const title = isEditing ? "Edit Event" : "Add a New Event";
    const description = isEditing
        ? "Make changes to your existing event here. Click save when you're done."
        : "Fill in the details below to create a new event for your organization.";

    const form = (
        <EventForm
            event={event}
            onSubmit={onSubmit}
        />
    );

    const footer = (
        <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>Cancel</Button>
            <Button type="submit" form={formId} disabled={isLoading}>
                {isLoading ? (isEditing ? "Saving..." : "Creating...") : (isEditing ? "Save Changes" : "Create Event")}
            </Button>
        </div>
    );

    if (isMobile) {
        return (
            <Drawer open={open} onOpenChange={onOpenChange}>
                <DrawerContent>
                    <DrawerHeader className="text-left">
                        <DrawerTitle>{title}</DrawerTitle>
                        <DrawerDescription>{description}</DrawerDescription>
                    </DrawerHeader>
                    <div className="overflow-y-auto max-h-[70vh] px-6">
                        {form}
                    </div>
                    <DrawerFooter className="pt-2 border-t">
                        {footer}
                    </DrawerFooter>
                </DrawerContent>
            </Drawer>
        );
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            {/* FIXED: Increased width to sm:max-w-2xl to fit side-by-side inputs */}
            <DialogContent className="sm:max-w-2xl grid grid-rows-[auto_minmax(0,1fr)_auto] p-0 max-h-[90vh]">
                <DialogHeader className="p-6 pb-4">
                    <DialogTitle>{title}</DialogTitle>
                    <DialogDescription>{description}</DialogDescription>
                </DialogHeader>
                <div className="overflow-y-auto px-6">
                    {form}
                </div>
                <DialogFooter className="p-6 pt-4 border-t">
                    {footer}
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
