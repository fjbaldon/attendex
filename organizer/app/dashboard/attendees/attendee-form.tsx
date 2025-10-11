"use client";

import {useForm} from "react-hook-form";
import {z} from "zod";
import {zodResolver} from "@hookform/resolvers/zod";
import {attendeeSchema} from "@/lib/schemas";
import {AttendeeResponse} from "@/types";
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage} from "@/components/ui/form";
import {Input} from "@/components/ui/input";
import {Button} from "@/components/ui/button";
import {Separator} from "@/components/ui/separator";
import {Skeleton} from "@/components/ui/skeleton";
import {IconUser, IconHash} from "@tabler/icons-react";
import {useCustomFields} from "@/hooks/use-custom-fields";
import {DynamicFormField} from "./dynamic-form-field";

type AttendeeFormValues = z.input<typeof attendeeSchema>;
type AttendeeFormSubmitValues = z.infer<typeof attendeeSchema>;

interface AttendeeFormProps {
    attendee?: AttendeeResponse | null;
    onSubmit: (values: AttendeeFormSubmitValues) => void;
    isLoading: boolean;
    onClose: () => void;
}

export function AttendeeForm({attendee, onSubmit, isLoading, onClose}: AttendeeFormProps) {
    const {definitions, isLoading: isLoadingDefinitions} = useCustomFields();

    const form = useForm({
        resolver: zodResolver(attendeeSchema),
        defaultValues: {
            uniqueIdentifier: attendee?.uniqueIdentifier || "",
            firstName: attendee?.firstName || "",
            lastName: attendee?.lastName || "",
            customFields: attendee?.customFields ? JSON.stringify(attendee.customFields, null, 2) : "",
        },
    });

    const isEditing = !!attendee;

    return (
        <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="grid gap-4">
                <FormField
                    control={form.control}
                    name="uniqueIdentifier"
                    render={({field}) => (
                        <FormItem>
                            <FormLabel>Unique Identifier</FormLabel>
                            <div className="relative">
                                <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                                    <IconHash className="h-4 w-4 text-muted-foreground"/>
                                </div>
                                <FormControl>
                                    <Input placeholder="e.g., 202012345" className="pl-10" {...field} />
                                </FormControl>
                            </div>
                            <FormMessage/>
                        </FormItem>
                    )}
                />
                <div className="grid grid-cols-2 gap-4">
                    <FormField
                        control={form.control}
                        name="firstName"
                        render={({field}) => (
                            <FormItem>
                                <FormLabel>First Name</FormLabel>
                                <div className="relative">
                                    <div
                                        className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                                        <IconUser className="h-4 w-4 text-muted-foreground"/>
                                    </div>
                                    <FormControl>
                                        <Input placeholder="John" className="pl-10" {...field} />
                                    </FormControl>
                                </div>
                                <FormMessage/>
                            </FormItem>
                        )}
                    />
                    <FormField
                        control={form.control}
                        name="lastName"
                        render={({field}) => (
                            <FormItem>
                                <FormLabel>Last Name</FormLabel>
                                <div className="relative">
                                    <div
                                        className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                                        <IconUser className="h-4 w-4 text-muted-foreground"/>
                                    </div>
                                    <FormControl>
                                        <Input placeholder="Doe" className="pl-10" {...field} />
                                    </FormControl>
                                </div>
                                <FormMessage/>
                            </FormItem>
                        )}
                    />
                </div>

                <Separator className="my-2"/>

                {isLoadingDefinitions ? (
                    <div className="space-y-4">
                        <Skeleton className="h-10 w-full"/>
                        <Skeleton className="h-10 w-full"/>
                    </div>
                ) : definitions.length > 0 ? (
                    <h3 className="text-sm font-medium text-muted-foreground">Custom Fields</h3>
                ) : null}

                {definitions.map((fieldDef) => (
                    <DynamicFormField<AttendeeFormValues>
                        key={fieldDef.id}
                        fieldDef={fieldDef}
                        control={form.control}
                        name={`customFields.${fieldDef.fieldName}`}
                    />
                ))}

                <div className="flex justify-end gap-2 pt-4">
                    <Button type="button" variant="outline" onClick={onClose}>Cancel</Button>
                    <Button type="submit" disabled={isLoading}>
                        {isLoading ? (isEditing ? "Saving..." : "Creating...") : (isEditing ? "Save Changes" : "Create Attendee")}
                    </Button>
                </div>
            </form>
        </Form>
    );
}
