"use client";

import {Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle} from "@/components/ui/dialog";
import {Drawer, DrawerContent, DrawerDescription, DrawerHeader, DrawerTitle} from "@/components/ui/drawer";
import {useIsMobile} from "@/hooks/use-mobile";
import {AttributeForm} from "./attribute-form";
import {Attribute} from "@/types";

interface AttributeDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    attribute: Attribute | null;
}

export function AttributeDialog({open, onOpenChange, attribute}: AttributeDialogProps) {
    const isMobile = useIsMobile();
    const isEditing = !!attribute;

    const title = isEditing ? "Edit Attribute" : "Add New Attribute";
    const description = isEditing
        ? "Update the options for this attribute. Note: name and type cannot be changed."
        : "Create a new attribute to capture additional attendee information.";

    const form = <AttributeForm attribute={attribute} onSuccess={() => onOpenChange(false)}/>;

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
            <DialogContent className="sm:max-w-md">
                <DialogHeader>
                    <DialogTitle>{title}</DialogTitle>
                    <DialogDescription>{description}</DialogDescription>
                </DialogHeader>
                {form}
            </DialogContent>
        </Dialog>
    );
}
