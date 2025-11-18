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
import {useStewards} from "@/hooks/use-stewards";
import {StewardCreateRequest} from "@/types";

interface AddStewardDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
}

const addStewardSchema = z.object({
    email: z.email("Please enter a valid email address."),
    password: z.string().min(8, "Password must be at least 8 characters long."),
});

export function AddStewardDialog({open, onOpenChange}: AddStewardDialogProps) {
    const {createSteward, isCreatingSteward} = useStewards();

    const form = useForm<z.infer<typeof addStewardSchema>>({
        resolver: zodResolver(addStewardSchema),
        defaultValues: {email: "", password: ""},
    });

    const onSubmit = (values: StewardCreateRequest) => {
        createSteward(values, {
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
                    <DialogTitle>Add New Steward</DialogTitle>
                    <DialogDescription>
                        This user will have full access to manage all organizations. They will be required to change
                        their password on first login.
                    </DialogDescription>
                </DialogHeader>
                <Form {...form}>
                    <form id="add-steward-form" onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
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
                            name="password" // CORRECTED: from temporaryPassword
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
                    <Button type="submit" form="add-steward-form" disabled={isCreatingSteward}>
                        {isCreatingSteward ? "Adding Steward..." : "Add Steward"}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
