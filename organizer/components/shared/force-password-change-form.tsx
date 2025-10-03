"use client";

import {useForm} from "react-hook-form";
import {z} from "zod";
import {zodResolver} from "@hookform/resolvers/zod";
import {passwordChangeSchema} from "@/lib/schemas";
import {useAuth} from "@/hooks/use-auth";
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from "@/components/ui/form";
import {Input} from "@/components/ui/input";
import {Button} from "@/components/ui/button";
import {useMutation} from "@tanstack/react-query";
import api from "@/lib/api";
import {toast} from "sonner";
import {AxiosError} from "axios";
import {useRouter} from "next/navigation";
import {useAuthStore} from "@/store/auth";

// ADDED: New component for the forced password change flow.
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

    const mutation = useMutation({
        mutationFn: (values: { newPassword: string }) =>
            api.post("/api/v1/users/me/change-password", values),
        onSuccess: () => {
            toast.success("Password changed successfully!", {
                description: "Logging you out. Please log in again with your new password.",
            });
            // For security, force a re-login after password change.
            clearToken();
            router.replace("/login");
        },
        onError: (error: AxiosError) => {
            toast.error("Failed to change password", {
                description: (error.response?.data as any)?.message || "An unknown error occurred.",
            });
        },
    });

    function onSubmit(values: z.infer<typeof passwordChangeSchema>) {
        mutation.mutate({newPassword: values.newPassword});
    }

    return (
        <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="grid gap-6">
                <FormField
                    control={form.control}
                    name="newPassword"
                    render={({field}) => (
                        <FormItem>
                            <FormLabel>New Password</FormLabel>
                            <FormControl>
                                <Input
                                    type="password"
                                    placeholder="********"
                                    {...field}
                                />
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
                            <FormLabel>Confirm New Password</FormLabel>
                            <FormControl>
                                <Input
                                    type="password"
                                    placeholder="********"
                                    {...field}
                                />
                            </FormControl>
                            <FormMessage/>
                        </FormItem>
                    )}
                />
                <div className="flex flex-col gap-4">
                    <Button type="submit" className="w-full" disabled={mutation.isPending}>
                        {mutation.isPending ? "Updating..." : "Set New Password"}
                    </Button>
                    <Button
                        type="button"
                        variant="outline"
                        className="w-full"
                        onClick={logout}
                    >
                        Cancel and Log Out
                    </Button>
                </div>
            </form>
        </Form>
    );
}
