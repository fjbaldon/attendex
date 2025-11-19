"use client";

import {useForm} from "react-hook-form";
import {z} from "zod";
import {zodResolver} from "@hookform/resolvers/zod";
import {userCreateSchema} from "@/lib/schemas";
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage} from "@/components/ui/form";
import {Input} from "@/components/ui/input";
import {Button} from "@/components/ui/button";
import {IconMail, IconKey} from "@tabler/icons-react";

interface OrganizerFormProps {
    onSubmit: (values: z.infer<typeof userCreateSchema>) => void;
    isLoading: boolean;
    onClose: () => void;
}

export function OrganizerForm({onSubmit, isLoading, onClose}: OrganizerFormProps) {
    const form = useForm({
        resolver: zodResolver(userCreateSchema),
        defaultValues: {
            email: "",
            password: "",
        },
    });

    return (
        <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="grid gap-4">
                <FormField
                    control={form.control}
                    name="email"
                    render={({field}) => (
                        <FormItem>
                            <FormLabel>Email Address</FormLabel>
                            <div className="relative">
                                <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                                    <IconMail className="h-4 w-4 text-muted-foreground"/>
                                </div>
                                <FormControl>
                                    <Input type="email" placeholder="name@example.com" className="pl-10" {...field} />
                                </FormControl>
                            </div>
                            <FormMessage/>
                        </FormItem>
                    )}
                />

                <FormField
                    control={form.control}
                    name="password"
                    render={({field}) => (
                        <FormItem>
                            <FormLabel>Temporary Password</FormLabel>
                            <div className="relative">
                                <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                                    <IconKey className="h-4 w-4 text-muted-foreground"/>
                                </div>
                                <FormControl>
                                    <Input type="password" placeholder="••••••••" className="pl-10" {...field} />
                                </FormControl>
                            </div>
                            <FormMessage/>
                        </FormItem>
                    )}
                />

                <div className="flex justify-end gap-2 pt-4">
                    <Button type="button" variant="outline" onClick={onClose}>Cancel</Button>
                    <Button type="submit" disabled={isLoading}>
                        {isLoading ? "Adding Organizer..." : "Add Organizer"}
                    </Button>
                </div>
            </form>
        </Form>
    );
}
