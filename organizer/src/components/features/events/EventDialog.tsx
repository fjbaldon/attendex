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
import type {Event} from "@/types";
import React from "react";

const formSchema = z.object({
    eventName: z.string().min(1, "Event name is required"),
    startDate: z.string().min(1, "Start date is required"),
    endDate: z.string().min(1, "End date is required"),
}).refine(data => new Date(data.startDate) <= new Date(data.endDate), {
    message: "End date cannot be before start date",
    path: ["endDate"],
});

interface EventDialogProps {
    event?: Event;
    onSave: (values: z.infer<typeof formSchema>) => void;
    children: React.ReactNode;
    open: boolean;
    onOpenChange: (open: boolean) => void;
}

export function EventDialog({event, onSave, children, open, onOpenChange}: EventDialogProps) {
    const form = useForm({
        resolver: zodResolver(formSchema),
        defaultValues: {
            eventName: event?.eventName || "",
            startDate: event?.startDate || "",
            endDate: event?.endDate || "",
        },
    });

    const handleSubmit = (values: z.infer<typeof formSchema>) => {
        onSave(values);
        onOpenChange(false);
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogTrigger asChild>{children}</DialogTrigger>
            <DialogContent className="sm:max-w-[425px]">
                <DialogHeader>
                    <DialogTitle>{event ? "Edit Event" : "Create Event"}</DialogTitle>
                </DialogHeader>
                <Form {...form}>
                    <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-4">
                        <FormField control={form.control} name="eventName" render={({field}) => (
                            <FormItem>
                                <FormLabel>Event Name</FormLabel>
                                <FormControl><Input {...field} /></FormControl>
                                <FormMessage/>
                            </FormItem>
                        )}/>
                        <FormField control={form.control} name="startDate" render={({field}) => (
                            <FormItem>
                                <FormLabel>Start Date</FormLabel>
                                <FormControl><Input type="date" {...field} /></FormControl>
                                <FormMessage/>
                            </FormItem>
                        )}/>
                        <FormField control={form.control} name="endDate" render={({field}) => (
                            <FormItem>
                                <FormLabel>End Date</FormLabel>
                                <FormControl><Input type="date" {...field} /></FormControl>
                                <FormMessage/>
                            </FormItem>
                        )}/>
                        <DialogFooter>
                            <DialogClose asChild>
                                <Button type="button" variant="secondary">Cancel</Button>
                            </DialogClose>
                            <Button type="submit">Save</Button>
                        </DialogFooter>
                    </form>
                </Form>
            </DialogContent>
        </Dialog>
    );
}
