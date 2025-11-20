"use client";

import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import {Button} from "@/components/ui/button";
import {useForm} from "react-hook-form";
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage} from "@/components/ui/form";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Organization} from "@/types";
import {useAdminOrganizations} from "@/hooks/use-admin-organizations";
import {useEffect} from "react";

type OrganizationLifecycle = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';

interface StatusDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    organization: Organization | null;
}

export function StatusDialog({open, onOpenChange, organization}: StatusDialogProps) {
    const {updateStatus, isUpdatingStatus} = useAdminOrganizations();
    const form = useForm({
        defaultValues: {
            lifecycle: organization?.lifecycle || 'INACTIVE',
        },
    });

    useEffect(() => {
        if (organization) {
            form.reset({
                lifecycle: organization.lifecycle,
            });
        }
    }, [organization, form]);

    const onSubmit = (values: { lifecycle: OrganizationLifecycle }) => {
        if (organization) {
            // Hook expects { status: ... } but backend expects lifecycle.
            // We map it here if the hook hasn't been updated, or update the hook.
            // Assuming the hook was generated to accept `status` but sends to backend.
            // For now, let's assume we pass what the form collects.

            // Note: You likely need to update hooks/use-admin-organizations.ts to accept 'lifecycle' too.
            // For this file context:
            updateStatus({id: organization.id, data: {lifecycle: values.lifecycle}}, {
                onSuccess: () => onOpenChange(false),
            });
        }
    };

    if (!organization) return null;

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Update Lifecycle for {organization.name}</DialogTitle>
                    <DialogDescription>
                        Change the access lifecycle of the organization. &#39;Inactive&#39; or &#39;Suspended&#39; will
                        prevent its users from logging in.
                    </DialogDescription>
                </DialogHeader>
                <Form {...form}>
                    <form id="status-form" onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                        <FormField
                            control={form.control}
                            name="lifecycle"
                            render={({field}) => (
                                <FormItem>
                                    <FormLabel>Lifecycle</FormLabel>
                                    <Select onValueChange={field.onChange} defaultValue={field.value}>
                                        <FormControl>
                                            <SelectTrigger>
                                                <SelectValue placeholder="Select a status"/>
                                            </SelectTrigger>
                                        </FormControl>
                                        <SelectContent>
                                            <SelectItem value="ACTIVE">Active</SelectItem>
                                            <SelectItem value="INACTIVE">Inactive</SelectItem>
                                            <SelectItem value="SUSPENDED">Suspended</SelectItem>
                                        </SelectContent>
                                    </Select>
                                    <FormMessage/>
                                </FormItem>
                            )}
                        />
                    </form>
                </Form>
                <DialogFooter>
                    <Button variant="outline" onClick={() => onOpenChange(false)}>Cancel</Button>
                    <Button type="submit" form="status-form" disabled={isUpdatingStatus}>
                        {isUpdatingStatus ? "Saving..." : "Save Changes"}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
