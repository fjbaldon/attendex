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
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import {
    Drawer,
    DrawerContent,
    DrawerDescription,
    DrawerHeader,
    DrawerTitle,
} from "@/components/ui/drawer";
import {EventForm} from "./event-form";

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

    const title = isEditing ? "Edit Event" : "Add a New Event";
    const description = isEditing
        ? "Make changes to your existing event here. Click save when you're done."
        : "Fill in the details below to create a new event for your organization.";

    const form = (
        <EventForm
            event={event}
            onSubmit={onSubmit}
            isLoading={isLoading}
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
            <DialogContent className="sm:max-w-[525px]">
                <DialogHeader>
                    <DialogTitle>{title}</DialogTitle>
                    <DialogDescription>{description}</DialogDescription>
                </DialogHeader>
                {form}
            </DialogContent>
        </Dialog>
    );
}
