"use client";

import {useForm} from "react-hook-form";
import {z} from "zod";
import {zodResolver} from "@hookform/resolvers/zod";
import {userRoleUpdateSchema} from "@/lib/schemas";
import {OrganizerResponse, RoleResponse} from "@/types";
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage} from "@/components/ui/form";
import {Button} from "@/components/ui/button";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {IconUserShield} from "@tabler/icons-react";

interface UserRoleFormProps {
    user: OrganizerResponse;
    roles: RoleResponse[];
    onSubmit: (values: z.infer<typeof userRoleUpdateSchema>) => void;
    isLoading: boolean;
    onClose: () => void;
}

export function UserRoleForm({user, roles, onSubmit, isLoading, onClose}: UserRoleFormProps) {
    const form = useForm({
        resolver: zodResolver(userRoleUpdateSchema),
        defaultValues: {
            roleId: user.roleId,
        },
    });

    return (
        <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="grid gap-4">
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

                <div className="flex justify-end gap-2 pt-4">
                    <Button type="button" variant="outline" onClick={onClose}>Cancel</Button>
                    <Button type="submit" disabled={isLoading}>
                        {isLoading ? "Saving..." : "Save Changes"}
                    </Button>
                </div>
            </form>
        </Form>
    );
}
