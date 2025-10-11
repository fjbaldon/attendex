"use client";

import * as React from "react";
import {z} from "zod";
import {userCreateSchema, userRoleUpdateSchema} from "@/lib/schemas";
import {useIsMobile} from "@/hooks/use-mobile";
import {OrganizerResponse, RoleResponse} from "@/types";
import {Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle} from "@/components/ui/dialog";
import {Drawer, DrawerContent, DrawerDescription, DrawerHeader, DrawerTitle} from "@/components/ui/drawer";
import {UserForm} from "./user-form";
import {UserRoleForm} from "./user-role-form";
import {useUsers} from "@/hooks/use-users";

interface UserDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    roles: RoleResponse[];
    user?: OrganizerResponse | null;
}

export function UserDialog({
                               open,
                               onOpenChange,
                               roles,
                               user,
                           }: UserDialogProps) {
    const isMobile = useIsMobile();
    const isEditing = !!user;

    const {createUser, isCreatingUser, updateUserRole, isUpdatingUserRole} = useUsers();

    const title = isEditing ? `Edit ${user.email}` : "Add a New User";
    const description = isEditing
        ? "Change the role for this team member."
        : "Fill in the details below to add a new organizer. They will be prompted to change their password on first login.";

    const handleCreateSubmit = (values: z.infer<typeof userCreateSchema>) => {
        createUser(values, {onSuccess: () => onOpenChange(false)});
    };

    const handleUpdateSubmit = (values: z.infer<typeof userRoleUpdateSchema>) => {
        if (user) {
            updateUserRole({id: user.id, data: values}, {
                onSuccess: () => onOpenChange(false),
            });
        }
    };


    const form = isEditing ? (
        <UserRoleForm
            user={user}
            roles={roles}
            onSubmit={handleUpdateSubmit}
            isLoading={isUpdatingUserRole}
            onClose={() => onOpenChange(false)}
        />
    ) : (
        <UserForm
            roles={roles}
            onSubmit={handleCreateSubmit}
            isLoading={isCreatingUser}
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
