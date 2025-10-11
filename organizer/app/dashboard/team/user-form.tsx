"use client";

import {useForm} from "react-hook-form";
import {z} from "zod";
import {zodResolver} from "@hookform/resolvers/zod";
import {userCreateSchema} from "@/lib/schemas";
import {RoleResponse} from "@/types";
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage} from "@/components/ui/form";
import {Input} from "@/components/ui/input";
import {Button} from "@/components/ui/button";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {IconMail, IconKey, IconUserShield} from "@tabler/icons-react";

interface UserFormProps {
    roles: RoleResponse[];
    onSubmit: (values: z.infer<typeof userCreateSchema>) => void;
    isLoading: boolean;
    onClose: () => void;
}

export function UserForm({roles, onSubmit, isLoading, onClose}: UserFormProps) {
    const form = useForm({
        resolver: zodResolver(userCreateSchema),
        defaultValues: {
            email: "",
            roleId: undefined,
            temporaryPassword: "",
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
                                    <Input
                                        type="email"
                                        placeholder="name@example.com"
                                        className="pl-10"
                                        {...field}
                                    />
                                </FormControl>
                            </div>
                            <FormMessage/>
                        </FormItem>
                    )}
                />

                <FormField
                    control={form.control}
                    name="roleId"
                    render={({field}) => (
                        <FormItem>
                            <FormLabel>Role</FormLabel>
                            <Select onValueChange={field.onChange} defaultValue={String(field.value)}>
                                <FormControl>
                                    <SelectTrigger>
                                        <IconUserShield className="mr-2 h-4 w-4 text-muted-foreground"/>
                                        <SelectValue placeholder="Select a role for the user"/>
                                    </SelectTrigger>
                                </FormControl>
                                <SelectContent>
                                    {roles.map((role) => (
                                        <SelectItem key={role.id} value={String(role.id)}>
                                            {role.name}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
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
                            <div className="relative">
                                <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                                    <IconKey className="h-4 w-4 text-muted-foreground"/>
                                </div>
                                <FormControl>
                                    <Input
                                        type="password"
                                        placeholder="••••••••"
                                        className="pl-10"
                                        {...field}
                                    />
                                </FormControl>
                            </div>
                            <FormMessage/>
                        </FormItem>
                    )}
                />

                <div className="flex justify-end gap-2 pt-4">
                    <Button type="button" variant="outline" onClick={onClose}>Cancel</Button>
                    <Button type="submit" disabled={isLoading}>
                        {isLoading ? "Sending Invite..." : "Add User"}
                    </Button>
                </div>
            </form>
        </Form>
    );
}
