"use client";

import {useEffect} from "react";
import {useForm} from "react-hook-form";
import {z} from "zod";
import {zodResolver} from "@hookform/resolvers/zod";
import {attendeeSchema} from "@/lib/schemas";
import {AttendeeResponse} from "@/types";
import {Form} from "@/components/ui/form";
import {Button} from "@/components/ui/button";
import {Separator} from "@/components/ui/separator";
import {Skeleton} from "@/components/ui/skeleton";
import {useCustomFields} from "@/hooks/use-custom-fields";
import {DynamicFormField, StandardFormField} from "./dynamic-form-field";

type AttendeeFormSubmitValues = z.infer<typeof attendeeSchema>;

interface AttendeeFormProps {
    attendee?: AttendeeResponse | null;
    onSubmit: (values: AttendeeFormSubmitValues) => void;
    isLoading: boolean;
    onClose: () => void;
}

export function AttendeeForm({attendee, onSubmit, isLoading, onClose}: AttendeeFormProps) {
    const {definitions, isLoading: isLoadingDefinitions} = useCustomFields();

    const form = useForm<AttendeeFormSubmitValues>({
        resolver: zodResolver(attendeeSchema),
    });

    useEffect(() => {
        const defaultCustomFields = definitions.reduce((acc, field) => {
            acc[field.fieldName] = attendee?.customFields?.[field.fieldName] ?? "";
            return acc;
        }, {} as Record<string, unknown>);

        form.reset({
            uniqueIdentifier: attendee?.uniqueIdentifier || "",
            firstName: attendee?.firstName || "",
            lastName: attendee?.lastName || "",
            customFields: defaultCustomFields,
        });
    }, [definitions, attendee, form]);


    const isEditing = !!attendee;

    return (
        <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="grid gap-4">
                <StandardFormField
                    control={form.control}
                    name="uniqueIdentifier"
                    label="Unique Identifier"
                    placeholder="e.g., 202012345"
                    iconType="hash"
                />
                {/* Reverted to the simpler, single-column layout */}
                <StandardFormField
                    control={form.control}
                    name="firstName"
                    label="First Name"
                    placeholder="John"
                    iconType="user"
                />
                <StandardFormField
                    control={form.control}
                    name="lastName"
                    label="Last Name"
                    placeholder="Doe"
                    iconType="user"
                />

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
                    <DynamicFormField
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
