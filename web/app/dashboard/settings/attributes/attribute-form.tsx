"use client";

import {useForm} from "react-hook-form";
import {z} from "zod";
import {zodResolver} from "@hookform/resolvers/zod";
import {attributeSchema} from "@/lib/schemas";
import {useAttributes} from "@/hooks/use-attributes";
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage} from "@/components/ui/form";
import {Input} from "@/components/ui/input";
import {Button} from "@/components/ui/button";
import {Attribute} from "@/types";

interface AttributeFormProps {
    onSuccess: () => void;
    attribute: Attribute | null;
}

export function AttributeForm({onSuccess, attribute}: AttributeFormProps) {
    const {createDefinition, isCreating, updateDefinition, isUpdating} = useAttributes();
    const isEditing = !!attribute;

    const form = useForm<z.infer<typeof attributeSchema>>({
        resolver: zodResolver(attributeSchema),
        defaultValues: {
            name: attribute?.name || "",
            options: attribute?.options?.join(', ') || "",
        },
    });

    const isLoading = isCreating || isUpdating;

    function onSubmit(values: z.infer<typeof attributeSchema>) {
        const requestData = {
            name: values.name,
            type: attribute?.type || "SELECT",
            options: values.options.split(',').map(opt => opt.trim()),
        };

        if (isEditing) {
            updateDefinition({attributeId: attribute.id, data: requestData}, {onSuccess});
        } else {
            createDefinition(requestData, {onSuccess});
        }
    }

    return (
        <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                <FormField
                    control={form.control}
                    name="name"
                    render={({field}) => (
                        <FormItem className="flex flex-col gap-1.5">
                            <FormLabel>Attribute Name</FormLabel>
                            <FormControl>
                                <Input placeholder="e.g., Department" {...field} />
                            </FormControl>
                            <FormMessage/>
                        </FormItem>
                    )}
                />

                <FormField
                    control={form.control}
                    name="options"
                    render={({field}) => (
                        <FormItem className="flex flex-col gap-1.5">
                            <FormLabel>Options (comma-separated)</FormLabel>
                            <FormControl>
                                <Input placeholder="Option A, Option B, Option C" {...field} />
                            </FormControl>
                            <FormMessage/>
                        </FormItem>
                    )}
                />


                <div className="flex justify-end pt-4">
                    <Button type="submit" disabled={isLoading} className="w-full sm:w-auto">
                        {isLoading ? "Saving..." : isEditing ? "Save Changes" : "Add Attribute"}
                    </Button>
                </div>
            </form>
        </Form>
    );
}
