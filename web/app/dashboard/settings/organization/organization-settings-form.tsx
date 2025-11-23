"use client";

import {useForm} from "react-hook-form";
import {z} from "zod";
import {zodResolver} from "@hookform/resolvers/zod";
import {organizationSettingsSchema} from "@/lib/schemas";
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage, FormDescription} from "@/components/ui/form";
import {Input} from "@/components/ui/input";
import {Button} from "@/components/ui/button";
import {Organization} from "@/types";
import {useEffect} from "react";

interface OrganizationSettingsFormProps {
    organization: Organization;
    onSubmit: (values: z.infer<typeof organizationSettingsSchema>) => void;
    isLoading: boolean;
}

export function OrganizationSettingsForm({organization, onSubmit, isLoading}: OrganizationSettingsFormProps) {
    const form = useForm<z.infer<typeof organizationSettingsSchema>>({
        resolver: zodResolver(organizationSettingsSchema),
        defaultValues: {
            name: "",
            identityFormatRegex: "",
        },
    });

    useEffect(() => {
        form.reset({
            name: organization.name,
            identityFormatRegex: organization.identityFormatRegex || "",
        });
    }, [organization, form]);

    return (
        <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                <FormField
                    control={form.control}
                    name="name"
                    render={({field}) => (
                        <FormItem>
                            <FormLabel>Organization Name</FormLabel>
                            <FormControl>
                                <Input placeholder="Your Organization Name" {...field} />
                            </FormControl>
                            <FormMessage/>
                        </FormItem>
                    )}
                />

                <FormField
                    control={form.control}
                    name="identityFormatRegex"
                    render={({field}) => (
                        <FormItem>
                            <FormLabel>Attendee Identifier Pattern (Optional)</FormLabel>
                            <FormControl>
                                <Input placeholder="e.g., ^[0-9]{9}$" {...field} />
                            </FormControl>
                            <FormDescription>
                                A regex pattern to validate attendee identifiers. For example, `^\d{9}$` enforces a
                                9-digit number.
                            </FormDescription>
                            <FormMessage/>
                        </FormItem>
                    )}
                />

                <div className="flex justify-end pt-2">
                    <Button type="submit" disabled={isLoading}>
                        {isLoading ? "Saving..." : "Save Changes"}
                    </Button>
                </div>
            </form>
        </Form>
    );
}
