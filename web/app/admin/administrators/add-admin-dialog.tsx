"use client";

import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import {Button} from "@/components/ui/button";
import {useForm} from "react-hook-form";
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage} from "@/components/ui/form";
import {Input} from "@/components/ui/input";
import {z} from "zod";
import {zodResolver} from "@hookform/resolvers/zod";
import {useSystemAdmins} from "@/hooks/use-system-admins";
import {SystemAdminCreateRequest} from "@/types";

interface AddAdminDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
}

const addAdminSchema = z.object({
    email: z.email("Please enter a valid email address."),
    temporaryPassword: z.string().min(8, "Password must be at least 8 characters long."),
});

export function AddAdminDialog({open, onOpenChange}: AddAdminDialogProps) {
    const {createAdmin, isCreatingAdmin} = useSystemAdmins();
    const form = useForm<z.infer<typeof addAdminSchema>>({
        resolver: zodResolver(addAdminSchema),
        defaultValues: {email: "", temporaryPassword: ""},
    });

    const onSubmit = (values: SystemAdminCreateRequest) => {
        createAdmin(values, {
            onSuccess: () => {
                onOpenChange(false);
                form.reset();
            },
        });
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Add New System Administrator</DialogTitle>
                    <DialogDescription>
                        This user will have full access to manage all organizations. They will be required to change
                        their password on first login.
                    </DialogDescription>
                </DialogHeader>
                <Form {...form}>
                    <form id="add-admin-form" onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                        <FormField
                            control={form.control}
                            name="email"
                            render={({field}) => (
                                <FormItem>
                                    <FormLabel>Email Address</FormLabel>
                                    <FormControl>
                                        <Input type="email" placeholder="admin@example.com" {...field} />
                                    </FormControl>
                                    <FormMessage/>
                                </FormItem>
                            )}
                        />
                        <FormField
                            control={form.control}
                            name="temporaryPassword"
                            render={({field}) => (
                                <FormItem>
                                    <FormLabel>Temporary Password</FormLabel>
                                    <FormControl>
                                        <Input type="password" placeholder="••••••••" {...field} />
                                    </FormControl>
                                    <FormMessage/>
                                </FormItem>
                            )}
                        />
                    </form>
                </Form>
                <DialogFooter>
                    <Button variant="outline" onClick={() => onOpenChange(false)}>Cancel</Button>
                    <Button type="submit" form="add-admin-form" disabled={isCreatingAdmin}>
                        {isCreatingAdmin ? "Adding Admin..." : "Add Admin"}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
