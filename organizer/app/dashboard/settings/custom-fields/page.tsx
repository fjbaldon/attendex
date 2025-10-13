"use client";

import {AppSidebar} from "@/components/layout/app-sidebar";
import {SiteHeader} from "@/components/layout/site-header";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {useForm} from "react-hook-form";
import {z} from "zod";
import {customFieldSchema} from "@/lib/schemas";
import {zodResolver} from "@hookform/resolvers/zod";
import {useCustomFields} from "@/hooks/use-custom-fields";
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage} from "@/components/ui/form";
import {Input} from "@/components/ui/input";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Button} from "@/components/ui/button";
import {Badge} from "@/components/ui/badge";
import {IconX} from "@tabler/icons-react";
import {Skeleton} from "@/components/ui/skeleton";

function AddCustomFieldForm() {
    const {createDefinition, isCreating} = useCustomFields();

    const form = useForm<z.infer<typeof customFieldSchema>>({
        resolver: zodResolver(customFieldSchema),
        defaultValues: {
            fieldName: "",
            fieldType: "TEXT",
            options: "",
        },
    });

    const fieldType = form.watch("fieldType");

    function onSubmit(values: z.infer<typeof customFieldSchema>) {
        const requestData = {
            fieldName: values.fieldName,
            fieldType: values.fieldType,
            options: values.fieldType === 'SELECT' && values.options
                ? values.options.split(',').map(opt => opt.trim())
                : undefined,
        };

        createDefinition(requestData, {
            onSuccess: () => form.reset(),
        });
    }

    return (
        <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="grid grid-cols-1 gap-4 sm:grid-cols-4">
                <FormField
                    control={form.control}
                    name="fieldName"
                    render={({field}) => (
                        <FormItem className="sm:col-span-2 flex flex-col gap-1.5">
                            <FormLabel>Field Name</FormLabel>
                            <FormControl>
                                <Input placeholder="e.g., Department" {...field} />
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
                            <Select onValueChange={field.onChange} defaultValue={field.value}>
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
                            <FormItem className="sm:col-span-4 flex flex-col gap-1.5">
                                <FormLabel>Options (comma-separated)</FormLabel>
                                <FormControl>
                                    <Input placeholder="Small, Medium, Large" {...field} />
                                </FormControl>
                                <FormMessage/>
                            </FormItem>
                        )}
                    />
                )}

                <div className="sm:col-span-4 flex justify-end">
                    <Button type="submit" disabled={isCreating} className="w-full sm:w-auto">
                        {isCreating ? "Adding..." : "Add Field"}
                    </Button>
                </div>
            </form>
        </Form>
    );
}

export default function CustomFieldsPage() {
    const {definitions, isLoading, deleteDefinition, isDeleting} = useCustomFields();

    return (
        <SidebarProvider
            style={
                {
                    "--sidebar-width": "calc(var(--spacing) * 72)",
                    "--header-height": "calc(var(--spacing) * 12)",
                } as React.CSSProperties
            }
        >
            <AppSidebar variant="inset"/>
            <SidebarInset>
                <SiteHeader title="Settings"/>
                <main className="flex flex-1 flex-col gap-4 p-4 lg:gap-6 lg:p-6">
                    <div className="flex items-center">
                        <div>
                            <h1 className="text-lg font-semibold md:text-2xl">Custom Fields</h1>
                            <p className="text-muted-foreground text-sm">
                                Define the custom data fields for all attendees in your organization.
                            </p>
                        </div>
                    </div>

                    <Card>
                        <CardHeader>
                            <CardTitle>Add New Field</CardTitle>
                            <CardDescription>
                                Create a new field that will appear on the attendee creation form.
                            </CardDescription>
                        </CardHeader>
                        <CardContent>
                            <AddCustomFieldForm/>
                        </CardContent>
                    </Card>

                    <Card>
                        <CardHeader>
                            <CardTitle>Existing Fields</CardTitle>
                            <CardDescription>
                                The following fields are currently defined for your organization.
                            </CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div className="space-y-4">
                                {isLoading ? (
                                    <>
                                        <Skeleton className="h-12 w-full"/>
                                        <Skeleton className="h-12 w-full"/>
                                    </>
                                ) : definitions.length > 0 ? (
                                    definitions.map(def => (
                                        <div key={def.id}
                                             className="flex items-center justify-between rounded-lg border p-3">
                                            <div className="flex flex-col gap-1">
                                                <span className="font-semibold">{def.fieldName}</span>
                                                <Badge variant="secondary" className="w-fit">{def.fieldType}</Badge>
                                            </div>
                                            <Button
                                                variant="ghost"
                                                size="icon"
                                                className="text-muted-foreground hover:text-destructive"
                                                onClick={() => deleteDefinition(def.id)}
                                                disabled={isDeleting}
                                            >
                                                <IconX className="h-4 w-4"/>
                                            </Button>
                                        </div>
                                    ))
                                ) : (
                                    <p className="text-sm text-muted-foreground text-center py-4">
                                        No custom fields have been defined yet.
                                    </p>
                                )}
                            </div>
                        </CardContent>
                    </Card>
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}
