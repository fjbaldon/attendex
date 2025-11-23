"use client";

import {useForm} from "react-hook-form";
import {z} from "zod";
import {zodResolver} from "@hookform/resolvers/zod";
import {passwordChangeSchema} from "@/lib/schemas";
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage} from "@/components/ui/form";
import {Input} from "@/components/ui/input";
import {Button} from "@/components/ui/button";
import {useMutation} from "@tanstack/react-query";
import api from "@/lib/api";
import {toast} from "sonner";
import {AxiosError} from "axios";
import {ApiErrorResponse} from "@/types";
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";

export function AccountPasswordForm() {
    const form = useForm<z.infer<typeof passwordChangeSchema>>({
        resolver: zodResolver(passwordChangeSchema),
        defaultValues: {
            newPassword: "",
            confirmPassword: "",
        },
    });

    const mutation = useMutation<void, AxiosError<ApiErrorResponse>, { newPassword: string }>({
        mutationFn: (values) =>
            api.put("/api/v1/users/me/password", values),
        onSuccess: () => {
            toast.success("Password updated successfully.");
            form.reset();
        },
        onError: (error) => {
            toast.error("Failed to update password", {
                description: error.response?.data?.message || "An unknown error occurred.",
            });
        },
    });

    function onSubmit(values: z.infer<typeof passwordChangeSchema>) {
        mutation.mutate({newPassword: values.newPassword});
    }

    return (
        <Card className="h-full flex flex-col">
            <CardHeader>
                <CardTitle>Security</CardTitle>
                <CardDescription>
                    Update your password associated with this account.
                </CardDescription>
            </CardHeader>
            <CardContent className="flex-1">
                <Form {...form}>
                    <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                        <FormField
                            control={form.control}
                            name="newPassword"
                            render={({field}) => (
                                <FormItem>
                                    <FormLabel>New Password</FormLabel>
                                    <FormControl>
                                        <Input type="password" placeholder="••••••••" {...field} />
                                    </FormControl>
                                    <FormMessage/>
                                </FormItem>
                            )}
                        />
                        <FormField
                            control={form.control}
                            name="confirmPassword"
                            render={({field}) => (
                                <FormItem>
                                    <FormLabel>Confirm Password</FormLabel>
                                    <FormControl>
                                        <Input type="password" placeholder="••••••••" {...field} />
                                    </FormControl>
                                    <FormMessage/>
                                </FormItem>
                            )}
                        />
                        <div className="flex justify-end pt-2">
                            <Button type="submit" disabled={mutation.isPending}>
                                {mutation.isPending ? "Updating..." : "Update Password"}
                            </Button>
                        </div>
                    </form>
                </Form>
            </CardContent>
        </Card>
    );
}
