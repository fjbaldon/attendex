"use client";

import {useForm} from "react-hook-form";
import {z} from "zod";
import {zodResolver} from "@hookform/resolvers/zod";
import {eventSchema} from "@/lib/schemas";
import {EventResponse} from "@/types";
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage} from "@/components/ui/form";
import {Input} from "@/components/ui/input";
import {Button} from "@/components/ui/button";
import {DateTimePicker} from "@/components/shared/date-time-picker";
import {TicketIcon} from "lucide-react";

interface EventFormProps {
    event?: EventResponse | null;
    onSubmit: (values: z.infer<typeof eventSchema>) => void;
    isLoading: boolean;
    onClose: () => void;
}

export function EventForm({event, onSubmit, isLoading, onClose}: EventFormProps) {
    const form = useForm({
        resolver: zodResolver(eventSchema),
        defaultValues: {
            eventName: event?.eventName || "",
            startDate: event ? new Date(event.startDate) : new Date(),
            endDate: event ? new Date(event.endDate) : new Date(),
        },
    });

    const startDateValue = form.watch("startDate");
    const isEditing = !!event;

    return (
        <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="grid gap-4">
                <FormField
                    control={form.control}
                    name="eventName"
                    render={({field}) => (
                        <FormItem>
                            <FormLabel>Event Name</FormLabel>
                            <div className="relative">
                                <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                                    <TicketIcon className="h-4 w-4 text-muted-foreground"/>
                                </div>
                                <FormControl>
                                    <Input
                                        placeholder="e.g., Annual Tech Conference"
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
                    name="startDate"
                    render={({field}) => (
                        <FormItem>
                            <FormLabel>Starts</FormLabel>
                            <DateTimePicker field={field}/>
                            <FormMessage/>
                        </FormItem>
                    )}
                />

                <FormField
                    control={form.control}
                    name="endDate"
                    render={({field}) => (
                        <FormItem>
                            <FormLabel>Ends</FormLabel>
                            <DateTimePicker
                                field={field}
                                disabledDays={{before: startDateValue}}
                            />
                            <FormMessage/>
                        </FormItem>
                    )}

                />

                <div className="flex justify-end gap-2 pt-4">
                    <Button type="button" variant="outline" onClick={onClose}>Cancel</Button>
                    <Button type="submit" disabled={isLoading}>
                        {isLoading ? (isEditing ? "Saving..." : "Creating...") : (isEditing ? "Save Changes" : "Create Event")}
                    </Button>
                </div>
            </form>
        </Form>
    );
}
