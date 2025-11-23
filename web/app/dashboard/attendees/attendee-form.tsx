"use client";

import {useForm} from "react-hook-form";
import {z} from "zod";
import {zodResolver} from "@hookform/resolvers/zod";
import {attendeeSchema} from "@/lib/schemas";
import {AttendeeResponse} from "@/types";
import {Form} from "@/components/ui/form";
import {Separator} from "@/components/ui/separator";
import {Skeleton} from "@/components/ui/skeleton";
import {useAttributes} from "@/hooks/use-attributes";
import {DynamicFormField, StandardFormField} from "./dynamic-form-field";

type AttendeeFormSubmitValues = z.infer<typeof attendeeSchema>;

interface AttendeeFormProps {
    attendee?: AttendeeResponse | null;
    onSubmit: (values: AttendeeFormSubmitValues) => void;
    formId: string;
}

export function AttendeeForm({attendee, onSubmit, formId}: AttendeeFormProps) {
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
            <form id={formId} onSubmit={form.handleSubmit(onSubmit)} className="grid grid-cols-2 gap-4">

                {/* Identity (Full Width) */}
                <div className="col-span-2">
                    <StandardFormField
                        control={form.control}
                        name="identity"
                        label="Identifier"
                        placeholder="e.g., 202012345"
                        iconType="hash"
                        disabled={isEditing}
                    />
                </div>

                {/* Names (50/50) */}
                <div className="col-span-1">
                    <StandardFormField
                        control={form.control}
                        name="firstName"
                        label="First Name"
                        placeholder="John"
                        iconType="user"
                    />
                </div>
                <div className="col-span-1">
                    <StandardFormField
                        control={form.control}
                        name="lastName"
                        label="Last Name"
                        placeholder="Doe"
                        iconType="user"
                    />
                </div>

                {/* Attributes Section */}
                {isLoadingAttributes ? (
                    <div className="col-span-2 space-y-4 pt-2">
                        <Separator />
                        <div className="grid grid-cols-2 gap-4">
                            <Skeleton className="h-[72px] w-full"/>
                            <Skeleton className="h-[72px] w-full"/>
                        </div>
                    </div>
                ) : attributes.length > 0 && (
                    <>
                        {/* Separator Row */}
                        <div className="col-span-2 py-2">
                            <Separator />
                        </div>

                        {/* Attributes Loop (Natural 2-column flow) */}
                        {attributes.map((attrDef) => (
                            <div key={attrDef.id} className="col-span-1">
                                <DynamicFormField
                                    fieldDef={attrDef}
                                    control={form.control}
                                    name={`attributes.${attrDef.name}`}
                                />
                            </div>
                        ))}
                    </>
                )}
            </form>
        </Form>
    );
}
