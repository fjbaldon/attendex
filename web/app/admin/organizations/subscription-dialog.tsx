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
import {Popover, PopoverContent, PopoverTrigger} from "@/components/ui/popover";
import {Calendar} from "@/components/ui/calendar";
import {CalendarIcon} from "lucide-react";
import {format} from "date-fns";
import {cn} from "@/lib/utils";
import {Organization} from "@/types";
import {useAdminOrganizations} from "@/hooks/use-admin-organizations";
import {useEffect} from "react";

type SubscriptionType = 'LIFETIME' | 'ANNUAL' | 'TRIAL';

interface SubscriptionDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    organization: Organization | null;
}

export function SubscriptionDialog({open, onOpenChange, organization}: SubscriptionDialogProps) {
    const {updateSubscription, isUpdatingSubscription} = useAdminOrganizations();
    const form = useForm({
        defaultValues: {
            subscriptionType: organization?.subscriptionType || 'TRIAL',
            subscriptionExpiresAt: organization?.subscriptionExpiresAt ? new Date(organization.subscriptionExpiresAt) : null,
        },
    });

    useEffect(() => {
        if (organization) {
            form.reset({
                subscriptionType: organization.subscriptionType,
                subscriptionExpiresAt: organization.subscriptionExpiresAt ? new Date(organization.subscriptionExpiresAt) : null,
            });
        }
    }, [organization, form]);

    const subscriptionType = form.watch("subscriptionType");

    const onSubmit = (values: { subscriptionType: SubscriptionType, subscriptionExpiresAt: Date | null }) => {
        if (organization) {
            // FIX: Map 'subscriptionExpiresAt' (Form) to 'expiresAt' (API)
            const data = {
                subscriptionType: values.subscriptionType,
                expiresAt: values.subscriptionType === 'LIFETIME' ? null : values.subscriptionExpiresAt,
            };
            updateSubscription({id: organization.id, data}, {
                onSuccess: () => onOpenChange(false),
            });
        }
    };

    if (!organization) return null;

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Update Subscription for {organization.name}</DialogTitle>
                    <DialogDescription>
                        Manage the organization&#39;s subscription plan and expiration date.
                    </DialogDescription>
                </DialogHeader>
                <Form {...form}>
                    <form id="subscription-form" onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                        <FormField
                            control={form.control}
                            name="subscriptionType"
                            render={({field}) => (
                                <FormItem>
                                    <FormLabel>Subscription Type</FormLabel>
                                    <Select onValueChange={field.onChange} defaultValue={field.value}>
                                        <FormControl>
                                            <SelectTrigger><SelectValue placeholder="Select a type"/></SelectTrigger>
                                        </FormControl>
                                        <SelectContent>
                                            <SelectItem value="LIFETIME">Lifetime</SelectItem>
                                            <SelectItem value="ANNUAL">Annual</SelectItem>
                                            <SelectItem value="TRIAL">Trial</SelectItem>
                                        </SelectContent>
                                    </Select>
                                    <FormMessage/>
                                </FormItem>
                            )}
                        />
                        {subscriptionType !== 'LIFETIME' && (
                            <FormField
                                control={form.control}
                                name="subscriptionExpiresAt"
                                render={({field}) => (
                                    <FormItem className="flex flex-col">
                                        <FormLabel>Expiration Date</FormLabel>
                                        <Popover>
                                            <PopoverTrigger asChild>
                                                <FormControl>
                                                    <Button
                                                        variant={"outline"}
                                                        className={cn("pl-3 text-left font-normal", !field.value && "text-muted-foreground")}
                                                    >
                                                        {field.value ? format(field.value, "PPP") :
                                                            <span>Pick a date</span>}
                                                        <CalendarIcon className="ml-auto h-4 w-4 opacity-50"/>
                                                    </Button>
                                                </FormControl>
                                            </PopoverTrigger>
                                            <PopoverContent className="w-auto p-0" align="start">
                                                <Calendar mode="single" selected={field.value ?? undefined}
                                                          onSelect={field.onChange}/>
                                            </PopoverContent>
                                        </Popover>
                                        <FormMessage/>
                                    </FormItem>
                                )}
                            />
                        )}
                    </form>
                </Form>
                <DialogFooter>
                    <Button variant="outline" onClick={() => onOpenChange(false)}>Cancel</Button>
                    <Button type="submit" form="subscription-form" disabled={isUpdatingSubscription}>
                        {isUpdatingSubscription ? "Saving..." : "Save Changes"}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
