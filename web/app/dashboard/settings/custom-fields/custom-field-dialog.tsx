"use client";

import {Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle} from "@/components/ui/dialog";
import {Drawer, DrawerContent, DrawerDescription, DrawerHeader, DrawerTitle} from "@/components/ui/drawer";
import {useIsMobile} from "@/hooks/use-mobile";
import {CustomFieldForm} from "./custom-field-form";
import {CustomFieldDefinition} from "@/types";

interface CustomFieldDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    field: CustomFieldDefinition | null;
}

export function CustomFieldDialog({open, onOpenChange, field}: CustomFieldDialogProps) {
    const isMobile = useIsMobile();
    const isEditing = !!field;

    const title = isEditing ? "Edit Custom Field" : "Add New Custom Field";
    const description = isEditing
        ? "Update the options for your custom field. Note: name and type cannot be changed."
        : "Create a new field to capture additional attendee information.";

    const form = <CustomFieldForm field={field} onSuccess={() => onOpenChange(false)}/>;

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
