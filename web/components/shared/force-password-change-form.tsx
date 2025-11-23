"use client";

import {useForm} from "react-hook-form";
import {z} from "zod";
import {zodResolver} from "@hookform/resolvers/zod";
import {passwordChangeSchema} from "@/lib/schemas";
import {useAuth} from "@/hooks/use-auth";
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage,} from "@/components/ui/form";
import {Input} from "@/components/ui/input";
import {Button} from "@/components/ui/button";
import {useMutation} from "@tanstack/react-query";
import api from "@/lib/api";
import {toast} from "sonner";
import {AxiosError} from "axios";
import {useRouter} from "next/navigation";
import {useAuthStore} from "@/store/auth";
import {Card, CardContent} from "@/components/ui/card";
import Image from "next/image";
import {ApiErrorResponse} from "@/types";
import {Logo} from "@/components/shared/logo";

export function ForcePasswordChangeForm() {
    const {logout} = useAuth();
    const router = useRouter();
    const {clearToken} = useAuthStore();

    const form = useForm<z.infer<typeof passwordChangeSchema>>({
        resolver: zodResolver(passwordChangeSchema),
        defaultValues: {
            newPassword: "",
            confirmPassword: "",
        },
    });

    const mutation = useMutation<void, AxiosError<ApiErrorResponse>, { newPassword: string }>({
        mutationFn: (values) =>
            api.post("/api/v1/users/me/change-password", values),
        onSuccess: () => {
            toast.success("Password Updated Successfully!", {
                description: "You have been logged out. Please sign in with your new password.",
            });
            clearToken();
            router.replace("/login");
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
        <Card className="overflow-hidden p-0 shadow-lg">
            <CardContent className="grid p-0 md:grid-cols-2">
                <div className="flex flex-col justify-center p-6 md:p-8">
                    <Form {...form}>
                        <form onSubmit={form.handleSubmit(onSubmit)} className="flex flex-col gap-6">
                            <div className="flex flex-col items-center text-center gap-2">
                                <div className="flex items-center gap-2 self-center font-semibold text-lg">
                                    <Logo className="size-7" />
                                    AttendEx
                                </div>
                                <h1 className="text-2xl font-bold">Create a New Password</h1>
                                <p className="text-muted-foreground text-balance">
                                    For your security, please create a new permanent password.
                                </p>
                            </div>
                            <FormField
                                control={form.control}
                                name="newPassword"
                                render={({field}) => (
                                    <FormItem className="grid gap-2 text-left">
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
                                    <FormItem className="grid gap-2 text-left">
                                        <FormLabel>Confirm Password</FormLabel>
                                        <FormControl>
                                            <Input type="password" placeholder="••••••••" {...field} />
                                        </FormControl>
                                        <FormMessage/>
                                    </FormItem>
                                )}
                            />
                            <div className="flex flex-col gap-2 pt-2">
                                <Button type="submit" className="w-full" disabled={mutation.isPending}>
                                    {mutation.isPending ? "Updating..." : "Set New Password"}
                                </Button>
                                <Button type="button" variant="outline" className="w-full" onClick={logout}>
                                    Cancel & Log Out
                                </Button>
                            </div>
                        </form>
                    </Form>
                </div>
                <div className="bg-muted relative hidden md:block">
                    <Image
                        src="https://images.unsplash.com/photo-1563013544-824ae1b704d3?q=80&w=2940&auto=format&fit=crop"
                        alt="Security lock on keyboard"
                        fill
                        className="absolute inset-0 h-full w-full object-cover dark:brightness-[0.3]"
                    />
                </div>
            </CardContent>
        </Card>
    );
}
