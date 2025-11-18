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

type OrganizationStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';

interface StatusDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    organization: Organization | null;
}

export function StatusDialog({open, onOpenChange, organization}: StatusDialogProps) {
    const {updateStatus, isUpdatingStatus} = useAdminOrganizations();
    const form = useForm({
        defaultValues: {
            status: organization?.status || 'INACTIVE',
        },
    });

    useEffect(() => {
        if (organization) {
            form.reset({
                status: organization.status,
            });
        }
    }, [organization, form]);

    const onSubmit = (values: { status: OrganizationStatus }) => {
        if (organization) {
            updateStatus({id: organization.id, data: values}, {
                onSuccess: () => onOpenChange(false),
            });
        }
    };

    if (!organization) return null;

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Update Status for {organization.name}</DialogTitle>
                    <DialogDescription>
                        Change the access status of the organization. &#39;Inactive&#39; or &#39;Suspended&#39; will
                        prevent its users
                        from logging in.
                    </DialogDescription>
                </DialogHeader>
                <Form {...form}>
                    <form id="status-form" onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                        <FormField
                            control={form.control}
                            name="status"
                            render={({field}) => (
                                <FormItem>
                                    <FormLabel>Status</FormLabel>
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
