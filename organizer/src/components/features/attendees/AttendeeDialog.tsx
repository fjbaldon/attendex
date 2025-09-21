import {useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import * as z from "zod";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage} from "@/components/ui/form";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
    DialogFooter,
    DialogClose
} from "@/components/ui/dialog";
import type {Attendee} from "@/types";
import React from "react";

const formSchema = z.object({
    schoolIdNumber: z.string().min(1, "School ID is required"),
    firstName: z.string().min(1, "First name is required"),
    middleInitial: z.string().max(1).optional(),
    lastName: z.string().min(1, "Last name is required"),
    course: z.string().optional(),
    yearLevel: z.coerce.number().optional(),
});

interface AttendeeDialogProps {
    attendee?: Attendee;
    onSave: (values: z.infer<typeof formSchema>) => void;
    children: React.ReactNode;
    open: boolean;
    onOpenChange: (open: boolean) => void;
}

export function AttendeeDialog({attendee, onSave, children, open, onOpenChange}: AttendeeDialogProps) {
    const form = useForm({
        resolver: zodResolver(formSchema),
        defaultValues: {
            schoolIdNumber: attendee?.schoolIdNumber || "",
            firstName: attendee?.firstName || "",
            middleInitial: attendee?.middleInitial || "",
            lastName: attendee?.lastName || "",
            course: attendee?.course || "",
            yearLevel: attendee?.yearLevel || undefined,
        },
    });

    const handleSubmit = (values: z.infer<typeof formSchema>) => {
        onSave(values);
        onOpenChange(false);
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogTrigger asChild>{children}</DialogTrigger>
            <DialogContent>
                <DialogHeader><DialogTitle>{attendee ? "Edit Attendee" : "Create Attendee"}</DialogTitle></DialogHeader>
                <Form {...form}>
                    <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-4">
                        <FormField control={form.control} name="schoolIdNumber" render={({field}) => (
                            <FormItem><FormLabel>School
                                ID</FormLabel><FormControl><Input {...field} /></FormControl><FormMessage/></FormItem>
                        )}/>
                        <FormField control={form.control} name="firstName" render={({field}) => (
                            <FormItem><FormLabel>First
                                Name</FormLabel><FormControl><Input {...field} /></FormControl><FormMessage/></FormItem>
                        )}/>
                        <FormField control={form.control} name="lastName" render={({field}) => (
                            <FormItem><FormLabel>Last
                                Name</FormLabel><FormControl><Input {...field} /></FormControl><FormMessage/></FormItem>
                        )}/>
                        <FormField control={form.control} name="middleInitial" render={({field}) => (
                            <FormItem><FormLabel>Middle
                                Initial</FormLabel><FormControl><Input {...field} /></FormControl><FormMessage/></FormItem>
                        )}/>
                        <FormField control={form.control} name="course" render={({field}) => (
                            <FormItem><FormLabel>Course</FormLabel><FormControl><Input {...field} /></FormControl><FormMessage/></FormItem>
                        )}/>
                        <FormField control={form.control} name="yearLevel" render={({field}) => (
                            <FormItem>
                                <FormLabel>Year Level</FormLabel>
                                <FormControl>
                                    <Input
                                        type="number"
                                        name={field.name}
                                        onBlur={field.onBlur}
                                        ref={field.ref}
                                        value={field.value !== undefined ? String(field.value) : ""}
                                        onChange={(event) => field.onChange(event.target.valueAsNumber)}
                                    />
                                </FormControl>
                                <FormMessage/>
                            </FormItem>
                        )}/>
                        <DialogFooter>
                            <DialogClose asChild><Button type="button" variant="secondary">Cancel</Button></DialogClose>
                            <Button type="submit">Save</Button>
                        </DialogFooter>
                    </form>
                </Form>
            </DialogContent>
        </Dialog>
    );
}
