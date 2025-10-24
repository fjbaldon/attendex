"use client";

import {useForm} from "react-hook-form";
import {z} from "zod";
import {zodResolver} from "@hookform/resolvers/zod";
import {customFieldSchema} from "@/lib/schemas";
import {useCustomFields} from "@/hooks/use-custom-fields";
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage} from "@/components/ui/form";
import {Input} from "@/components/ui/input";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Button} from "@/components/ui/button";
import {CustomFieldDefinition} from "@/types";

interface CustomFieldFormProps {
    onSuccess: () => void;
    field: CustomFieldDefinition | null;
}

export function CustomFieldForm({onSuccess, field}: CustomFieldFormProps) {
    const {createDefinition, isCreating, updateDefinition, isUpdating} = useCustomFields();
    const isEditing = !!field;

    const form = useForm<z.infer<typeof customFieldSchema>>({
        resolver: zodResolver(customFieldSchema),
        defaultValues: {
            fieldName: field?.fieldName || "",
            fieldType: field?.fieldType || "TEXT",
            options: field?.options?.join(', ') || "",
        },
    });

    const fieldType = form.watch("fieldType");
    const isLoading = isCreating || isUpdating;

    function onSubmit(values: z.infer<typeof customFieldSchema>) {
        const requestData = {
            fieldName: values.fieldName,
            fieldType: values.fieldType,
            options: values.fieldType === 'SELECT' && values.options
                ? values.options.split(',').map(opt => opt.trim())
                : undefined,
        };

        if (isEditing) {
            updateDefinition({fieldId: field.id, data: requestData}, {onSuccess});
        } else {
            createDefinition(requestData, {onSuccess});
        }
    }

    return (
        <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                <FormField
                    control={form.control}
                    name="fieldName"
                    render={({field}) => (
                        <FormItem className="flex flex-col gap-1.5">
                            <FormLabel>Field Name</FormLabel>
                            <FormControl>
                                <Input placeholder="e.g., Department" {...field} disabled={isEditing}/>
                            </FormControl>
                            <FormMessage/>
                        </FormItem>
                    )}
                />

                <FormField
                    control={form.control}
                    name="fieldType"
                    render={({field}) => (
                        <FormItem className="flex flex-col gap-1.5">
                            <FormLabel>Field Type</FormLabel>
                            <Select onValueChange={field.onChange} defaultValue={field.value} disabled={isEditing}>
                                <FormControl>
                                    <SelectTrigger>
                                        <SelectValue placeholder="Select a type"/>
                                    </SelectTrigger>
                                </FormControl>
                                <SelectContent>
                                    <SelectItem value="TEXT">Text</SelectItem>
                                    <SelectItem value="NUMBER">Number</SelectItem>
                                    <SelectItem value="DATE">Date</SelectItem>
                                    <SelectItem value="SELECT">Select</SelectItem>
                                </SelectContent>
                            </Select>
                            <FormMessage/>
                        </FormItem>
                    )}
                />

                {fieldType === 'SELECT' && (
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
                )}

                <div className="flex justify-end pt-4">
                    <Button type="submit" disabled={isLoading} className="w-full sm:w-auto">
                        {isLoading ? "Saving..." : isEditing ? "Save Changes" : "Add Field"}
                    </Button>
                </div>
            </form>
        </Form>
    );
}
