"use client";

import {z} from "zod";
import {useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import {passwordResetSchema} from "@/lib/schemas";
import {Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle} from "@/components/ui/dialog";
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage} from "@/components/ui/form";
import {Input} from "@/components/ui/input";
import {Button} from "@/components/ui/button";
import {IconKey} from "@tabler/icons-react";

interface UserForPasswordReset {
    id: number;
    email: string;
}

interface ResetPasswordDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    user: UserForPasswordReset | null;
    onSubmit: (values: { userId: number, newTemporaryPassword: string }) => void;
    isLoading: boolean;
}

export function ResetPasswordDialog({open, onOpenChange, user, onSubmit, isLoading}: ResetPasswordDialogProps) {
    const form = useForm({
        resolver: zodResolver(passwordResetSchema),
        defaultValues: {
            newTemporaryPassword: "",
            confirmPassword: "",
        },
    });

    const handleSubmit = (values: z.infer<typeof passwordResetSchema>) => {
        if (user) {
            onSubmit({
                userId: user.id,
                newTemporaryPassword: values.newTemporaryPassword,
            });
        }
    };

    if (!user) return null;

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-md">
                <DialogHeader>
                    <DialogTitle>Reset Password</DialogTitle>
                    <DialogDescription>
                        Set a new temporary password for <span className="font-medium">{user.email}</span>.
                    </DialogDescription>
                </DialogHeader>
                <Form {...form}>
                    <form onSubmit={form.handleSubmit(handleSubmit)} className="grid gap-4">
                        <FormField
                            control={form.control}
                            name="newTemporaryPassword"
                            render={({field}) => (
                                <FormItem>
                                    <FormLabel>New Temporary Password</FormLabel>
                                    <div className="relative">
                                        <div
                                            className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                                            <IconKey className="h-4 w-4 text-muted-foreground"/>
                                        </div>
                                        <FormControl>
                                            <Input type="password" placeholder="••••••••"
                                                   className="pl-10" {...field} />
                                        </FormControl>
                                    </div>
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
                                    <div className="relative">
                                        <div
                                            className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                                            <IconKey className="h-4 w-4 text-muted-foreground"/>
                                        </div>
                                        <FormControl>
                                            <Input type="password" placeholder="••••••••"
                                                   className="pl-10" {...field} />
                                        </FormControl>
                                    </div>
                                    <FormMessage/>
                                </FormItem>
                            )}
                        />
                        <div className="flex justify-end gap-2 pt-4">
                            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>Cancel</Button>
                            <Button type="submit" disabled={isLoading}>
                                {isLoading ? "Resetting..." : "Reset Password"}
                            </Button>
                        </div>
                    </form>
                </Form>
            </DialogContent>
        </Dialog>
    );
}
