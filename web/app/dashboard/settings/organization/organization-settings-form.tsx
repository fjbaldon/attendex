"use client";

import {useForm} from "react-hook-form";
import {z} from "zod";
import {zodResolver} from "@hookform/resolvers/zod";
import {organizationSettingsSchema} from "@/lib/schemas";
import {Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage} from "@/components/ui/form";
import {Input} from "@/components/ui/input";
import {Button} from "@/components/ui/button";
import {Organization} from "@/types";
import {useEffect, useState} from "react";
import {Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle} from "@/components/ui/card";
import {Badge} from "@/components/ui/badge";
import {CheckCircle2, XCircle} from "lucide-react";

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

    const regexValue = form.watch("identityFormatRegex");
    const [testId, setTestId] = useState("");
    const [isMatch, setIsMatch] = useState<boolean | null>(null);

    useEffect(() => {
        form.reset({
            name: organization.name,
            identityFormatRegex: organization.identityFormatRegex || "",
        });
    }, [organization, form]);

    useEffect(() => {
        if (!regexValue || !testId) {
            setIsMatch(null);
            return;
        }
        try {
            const regex = new RegExp(regexValue);
            setIsMatch(regex.test(testId));
        } catch {
            setIsMatch(false);
        }
    }, [regexValue, testId]);

    return (
        <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">

                <Card>
                    <CardHeader>
                        <CardTitle>General Information</CardTitle>
                        <CardDescription>Visible details about your organization.</CardDescription>
                    </CardHeader>
                    <CardContent>
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
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>Identity Validation</CardTitle>
                        <CardDescription>Enforce a specific format for attendee IDs to prevent scanning errors.</CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-6">
                        <FormField
                            control={form.control}
                            name="identityFormatRegex"
                            render={({field}) => (
                                <FormItem>
                                    <FormLabel>ID Format Pattern (Regex)</FormLabel>
                                    <div className="flex gap-2">
                                        <FormControl>
                                            {/* We use double backslash here to show it correctly as a string example */}
                                            <Input className="font-mono text-sm" placeholder="e.g., ^\d{9}$" {...field} />
                                        </FormControl>
                                    </div>
                                    <FormDescription>
                                        {/* FIXED: Passed as a string literal {"..."} so JSX treats braces as text */}
                                        Leave blank to allow any format. Example: <code className="bg-muted px-1 rounded">{"^\\d{9}$"}</code> enforces exactly 9 digits.
                                    </FormDescription>
                                    <FormMessage/>
                                </FormItem>
                            )}
                        />

                        <div className="bg-muted/50 p-4 rounded-lg border space-y-2">
                            <FormLabel className="text-xs text-muted-foreground uppercase tracking-wider">Pattern Tester</FormLabel>
                            <div className="flex items-center gap-3">
                                <Input
                                    placeholder="Type a sample ID to test..."
                                    value={testId}
                                    onChange={(e) => setTestId(e.target.value)}
                                    className="max-w-xs bg-background"
                                />
                                {testId && (
                                    <div className="flex items-center gap-2">
                                        {isMatch ? (
                                            <Badge variant="default" className="bg-green-600 hover:bg-green-600 gap-1">
                                                <CheckCircle2 className="w-3 h-3" /> Valid
                                            </Badge>
                                        ) : (
                                            <Badge variant="destructive" className="gap-1">
                                                <XCircle className="w-3 h-3" /> Invalid
                                            </Badge>
                                        )}
                                    </div>
                                )}
                            </div>
                            <p className="text-[0.8rem] text-muted-foreground">
                                {isMatch
                                    ? `Great! "${testId}" matches your pattern.`
                                    : testId && "This ID does not match the pattern above."}
                            </p>
                        </div>
                    </CardContent>
                    <CardFooter className="border-t bg-muted/5 px-6 py-4">
                        <Button type="submit" disabled={isLoading}>
                            {isLoading ? "Saving..." : "Save Changes"}
                        </Button>
                    </CardFooter>
                </Card>
            </form>
        </Form>
    );
}
