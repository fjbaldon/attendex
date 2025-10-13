"use client";

import * as React from "react";
import {z} from "zod";
import {userCreateSchema} from "@/lib/schemas";
import {useIsMobile} from "@/hooks/use-mobile";
import {Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle} from "@/components/ui/dialog";
import {Drawer, DrawerContent, DrawerDescription, DrawerHeader, DrawerTitle} from "@/components/ui/drawer";
import {ScannerForm} from "./scanner-form";
import {useScanners} from "@/hooks/use-scanners";

interface ScannerDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
}

export function ScannerDialog({open, onOpenChange}: ScannerDialogProps) {
    const isMobile = useIsMobile();
    const {createScanner, isCreatingScanner} = useScanners();

    const title = "Add New Scanner";
    const description = "Create a new account for use with the mobile scanner app. The user will be prompted to change their password on first login.";

    const handleSubmit = (values: z.infer<typeof userCreateSchema>) => {
        createScanner(values, {
            onSuccess: () => onOpenChange(false),
        });
    };

    const form = (
        <ScannerForm
            onSubmit={handleSubmit}
            isLoading={isCreatingScanner}
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
