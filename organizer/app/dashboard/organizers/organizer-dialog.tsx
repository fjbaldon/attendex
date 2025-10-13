"use client";

import * as React from "react";
import {z} from "zod";
import {userCreateSchema} from "@/lib/schemas";
import {useIsMobile} from "@/hooks/use-mobile";
import {Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle} from "@/components/ui/dialog";
import {Drawer, DrawerContent, DrawerDescription, DrawerHeader, DrawerTitle} from "@/components/ui/drawer";
import {OrganizerForm} from "./organizer-form";
import {useOrganizers} from "@/hooks/use-organizers";

interface OrganizerDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
}

export function OrganizerDialog({open, onOpenChange}: OrganizerDialogProps) {
    const isMobile = useIsMobile();
    const {createOrganizer, isCreatingOrganizer} = useOrganizers();

    const title = "Add New Organizer";
    const description = "Create an account for a new organizer. They will be prompted to change their password on first login.";

    const handleSubmit = (values: z.infer<typeof userCreateSchema>) => {
        createOrganizer(values, {
            onSuccess: () => onOpenChange(false),
        });
    };

    const form = (
        <OrganizerForm
            onSubmit={handleSubmit}
            isLoading={isCreatingOrganizer}
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
            <DialogContent className="sm:max-w-[425px]">
                <DialogHeader>
                    <DialogTitle>{title}</DialogTitle>
                    <DialogDescription>{description}</DialogDescription>
                </DialogHeader>
                {form}
            </DialogContent>
        </Dialog>
    );
}
