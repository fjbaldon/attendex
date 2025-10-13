"use client";

import {useForm} from "react-hook-form";
import {z} from "zod";
import {zodResolver} from "@hookform/resolvers/zod";
import {organizationSettingsSchema} from "@/lib/schemas";
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage, FormDescription} from "@/components/ui/form";
import {Input} from "@/components/ui/input";
import {Button} from "@/components/ui/button";
import {Organization} from "@/types";
import {useEffect, useState} from "react";
import {toast} from "sonner";
import {Separator} from "@/components/ui/separator";

interface OrganizationSettingsFormProps {
    organization: Organization;
    onSubmit: (values: z.infer<typeof organizationSettingsSchema>) => void;
    isLoading: boolean;
}

export function OrganizationSettingsForm({organization, onSubmit, isLoading}: OrganizationSettingsFormProps) {
    const [testIdentifier, setTestIdentifier] = useState("");

    const form = useForm<z.infer<typeof organizationSettingsSchema>>({
        resolver: zodResolver(organizationSettingsSchema),
        defaultValues: {
            name: "",
            identifierFormatRegex: "",
        },
    });

    useEffect(() => {
        form.reset({
            name: organization.name,
            identifierFormatRegex: organization.identifierFormatRegex || "",
        });
    }, [organization, form]);

    const handleTestPattern = () => {
        const pattern = form.getValues("identifierFormatRegex");
        if (!pattern) {
            toast.info("Pattern is empty", {
                description: "Enter a pattern above to test it."
            });
            return;
        }

        try {
            const regex = new RegExp(pattern);
            if (regex.test(testIdentifier)) {
                toast.success("Pattern matched!", {
                    description: `✅ '${testIdentifier}' matches the pattern.`
                });
            } else {
                toast.error("Pattern did not match", {
                    description: `❌ '${testIdentifier}' does not match the pattern.`
                });
            }
        } catch {
            toast.error("Invalid Regex Pattern", {
                description: "The pattern you entered is not a valid regular expression."
            });
        }
    };

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
                    name="identifierFormatRegex"
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

                <Separator/>

                <div className="space-y-3 rounded-lg border bg-muted/50 p-4">
                    <h3 className="font-medium">Test Your Pattern</h3>
                    <div className="flex items-center gap-2">
                        <Input
                            placeholder="Enter a sample identifier..."
                            value={testIdentifier}
                            onChange={(e) => setTestIdentifier(e.target.value)}
                        />
                        <Button type="button" variant="secondary" onClick={handleTestPattern}>
                            Test
                        </Button>
                    </div>
                </div>


                <div className="flex justify-end pt-2">
                    <Button type="submit" disabled={isLoading}>
                        {isLoading ? "Saving..." : "Save Changes"}
                    </Button>
                </div>
            </form>
        </Form>
    );
}
