"use client";

import {useForm} from "react-hook-form";
import {z} from "zod";
import {zodResolver} from "@hookform/resolvers/zod";
import {attendeeSchema} from "@/lib/schemas";
import {AttendeeResponse} from "@/types";
import {Form} from "@/components/ui/form";
import {Button} from "@/components/ui/button";
import {Separator} from "@/components/ui/separator";
import {Skeleton} from "@/components/ui/skeleton";
import {useAttributes} from "@/hooks/use-attributes";
import {DynamicFormField, StandardFormField} from "./dynamic-form-field";

type AttendeeFormSubmitValues = z.infer<typeof attendeeSchema>;

interface AttendeeFormProps {
    attendee?: AttendeeResponse | null;
    onSubmit: (values: AttendeeFormSubmitValues) => void;
    isLoading: boolean;
    onClose: () => void;
}

export function AttendeeForm({attendee, onSubmit, isLoading, onClose}: AttendeeFormProps) {
    const {definitions: attributes, isLoading: isLoadingAttributes} = useAttributes();
    const isEditing = !!attendee;

    const defaultAttributes = attributes.reduce((acc, attr) => {
        acc[attr.name] = attendee?.attributes?.[attr.name] ?? "";
        return acc;
    }, {} as Record<string, unknown>);

    const form = useForm<AttendeeFormSubmitValues>({
        resolver: zodResolver(attendeeSchema),
        defaultValues: {
            identity: attendee?.identity || "",
            firstName: attendee?.firstName || "",
            lastName: attendee?.lastName || "",
            attributes: defaultAttributes,
        },
    });

    return (
        <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="grid gap-4">
                <StandardFormField
                    control={form.control}
                    name="identity"
                    label="Identifier"
                    placeholder="e.g., 202012345"
                    iconType="hash"
                    disabled={isEditing}
                />
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

                {isLoadingAttributes ? (
                    <div className="space-y-4">
                        <Skeleton className="h-10 w-full"/>
                        <Skeleton className="h-10 w-full"/>
                    </div>
                ) : attributes.length > 0 ? (
                    <h3 className="text-sm font-medium text-muted-foreground">Attributes</h3>
                ) : null}

                {attributes.map((attrDef) => (
                    <DynamicFormField
                        key={attrDef.id}
                        fieldDef={attrDef}
                        control={form.control}
                        name={`attributes.${attrDef.name}`}
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
