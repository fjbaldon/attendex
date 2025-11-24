"use client";

import {useFieldArray, useForm} from "react-hook-form";
import {z} from "zod";
import {zodResolver} from "@hookform/resolvers/zod";
import {eventSchema} from "@/lib/schemas";
import {EventResponse} from "@/types";
import {Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage} from "@/components/ui/form";
import {Input} from "@/components/ui/input";
import {Button} from "@/components/ui/button";
import {IconCalendar, IconPlus, IconTrash} from "@tabler/icons-react";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Separator} from "@/components/ui/separator";
import {Popover, PopoverContent, PopoverTrigger} from "@/components/ui/popover";
import {cn} from "@/lib/utils";
import {endOfDay, format, startOfDay} from "date-fns";
import * as React from "react";
import {useEffect} from "react";
import {Calendar} from "@/components/ui/calendar";
import {DateTimePicker} from "@/components/shared/date-time-picker";

type EventFormValues = z.infer<typeof eventSchema>;

interface EventFormProps {
    event?: EventResponse | null;
    onSubmit: (values: EventFormValues) => void;
}

export function EventForm({event, onSubmit}: EventFormProps) {
    const isEditing = !!event;
    const isDateEditable = !isEditing || event?.status === "UPCOMING";

    const defaultValues = React.useMemo(() => {
        if (event) {
            return {
                name: event.name || "",
                startDate: new Date(event.startDate),
                endDate: new Date(event.endDate),
                graceMinutesBefore: event.graceMinutesBefore,
                graceMinutesAfter: event.graceMinutesAfter,
                sessions: event.sessions.map(s => ({
                    id: s.id,
                    activityName: s.activityName,
                    targetTime: new Date(s.targetTime),
                    intent: s.intent
                }))
            };
        }
        const today = new Date();
        return {
            name: "",
            startDate: startOfDay(today),
            endDate: endOfDay(today),
            graceMinutesBefore: 30,
            graceMinutesAfter: 30,
            sessions: [],
        };
    }, [event]);

    const form = useForm({
        resolver: zodResolver(eventSchema),
        mode: "onChange",
        defaultValues: defaultValues,
    });

    // FIX: Only reset if the form is pristine (not dirty).
    // This prevents overwriting user input if a background refetch updates the 'event' prop.
    useEffect(() => {
        if (!form.formState.isDirty) {
            form.reset(defaultValues);
        }
    }, [defaultValues, form]);

    const {fields, append, remove} = useFieldArray({
        control: form.control,
        name: "sessions",
    });

    const startDate = form.watch("startDate");
    const endDate = form.watch("endDate");

    React.useEffect(() => {
        // Trigger validation for sessions if dates change
        if (fields.length > 0 && startDate && endDate) {
            void form.trigger("sessions");
        }
    }, [startDate, endDate, form, fields.length]);

    return (
        <Form {...form}>
            <form id="event-form" onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                <FormField
                    control={form.control} name="name" render={({field}) => (
                    <FormItem>
                        <FormLabel>Event Name</FormLabel>
                        <FormControl><Input placeholder="e.g., Annual Tech Conference" {...field} /></FormControl>
                        <FormMessage/>
                    </FormItem>
                )}/>
                <div className="grid grid-cols-2 gap-4">
                    <FormField control={form.control} name="startDate" render={({field}) => (
                        <FormItem className="flex flex-col">
                            <FormLabel>Event Start Date</FormLabel>
                            <Popover>
                                <PopoverTrigger asChild>
                                    <FormControl>
                                        <Button variant={"outline"}
                                                disabled={!isDateEditable}
                                                className={cn("pl-3 text-left font-normal", !field.value && "text-muted-foreground")}>
                                            {field.value ? format(field.value, "PPP") : (<span>Pick a date</span>)}
                                            <IconCalendar className="ml-auto h-4 w-4 opacity-50"/>
                                        </Button>
                                    </FormControl>
                                </PopoverTrigger>
                                <PopoverContent className="w-auto p-0" align="start">
                                    <Calendar
                                        mode="single"
                                        selected={field.value}
                                        onSelect={(date) => date && field.onChange(startOfDay(date))}
                                        autoFocus
                                    />
                                </PopoverContent>
                            </Popover>
                            <FormMessage/>
                        </FormItem>
                    )}/>
                    <FormField control={form.control} name="endDate" render={({field}) => (
                        <FormItem className="flex flex-col">
                            <FormLabel>Event End Date</FormLabel>
                            <Popover>
                                <PopoverTrigger asChild>
                                    <FormControl>
                                        <Button variant={"outline"}
                                                disabled={!isDateEditable}
                                                className={cn("pl-3 text-left font-normal", !field.value && "text-muted-foreground")}>
                                            {field.value ? format(field.value, "PPP") : (<span>Pick a date</span>)}
                                            <IconCalendar className="ml-auto h-4 w-4 opacity-50"/>
                                        </Button>
                                    </FormControl>
                                </PopoverTrigger>
                                <PopoverContent className="w-auto p-0" align="start">
                                    <Calendar
                                        mode="single"
                                        selected={field.value}
                                        onSelect={(date) => date && field.onChange(endOfDay(date))}
                                        disabled={form.getValues("startDate") ? {before: startOfDay(form.getValues("startDate"))} : undefined}
                                        autoFocus
                                    />
                                </PopoverContent>
                            </Popover>
                            <FormMessage/>
                        </FormItem>
                    )}/>
                </div>

                {!isDateEditable && (
                    <p className="text-xs text-muted-foreground">
                        Event dates cannot be changed once the event day has arrived.
                    </p>
                )}

                <div className="space-y-2 pt-2">
                    <FormLabel>On-Time Window</FormLabel>
                    <FormDescription>Define the grace period around a session&#39;s target time to be considered
                        &#34;Punctual&#34;.</FormDescription>
                    <div className="grid grid-cols-2 gap-4">
                        <FormField control={form.control} name="graceMinutesBefore" render={({field}) => (
                            <FormItem>
                                <FormLabel className="text-xs font-normal">Minutes Before Target</FormLabel>
                                <FormControl>
                                    <Input type="number" min="0" {...field} value={field.value as string}/>
                                </FormControl>
                                <FormMessage/>
                            </FormItem>
                        )}/>
                        <FormField control={form.control} name="graceMinutesAfter" render={({field}) => (
                            <FormItem>
                                <FormLabel className="text-xs font-normal">Minutes After Target</FormLabel>
                                <FormControl>
                                    <Input type="number" min="0" {...field} value={field.value as string}/>
                                </FormControl>
                                <FormMessage/>
                            </FormItem>
                        )}/>
                    </div>
                </div>

                <Separator className="my-4"/>

                <div className="space-y-1">
                    <FormLabel>Event Sessions</FormLabel>
                    <p className="text-sm text-muted-foreground">Define key moments for your event like arrivals or
                        departures.</p>
                    {form.formState.errors.sessions?.root && (
                        <p className="text-sm font-medium text-destructive mt-2">
                            {form.formState.errors.sessions.root.message}
                        </p>
                    )}
                </div>

                <div className="space-y-4">
                    {fields.map((field, index) => (
                        <div key={field.id} className="rounded-lg border bg-muted/50 p-4 space-y-4 relative">
                            <Button type="button" variant="ghost" size="icon"
                                    onClick={() => remove(index)}
                                    className="text-muted-foreground hover:text-destructive absolute top-2 right-2 h-7 w-7">
                                <IconTrash className="h-4 w-4"/>
                            </Button>
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-4 gap-y-2 items-start">
                                <FormField control={form.control}
                                           name={`sessions.${index}.activityName`}
                                           render={({field}) => (
                                               <FormItem>
                                                   <FormLabel className="text-xs">Activity Name</FormLabel>
                                                   <FormControl><Input
                                                       placeholder="e.g., Morning Registration" {...field} /></FormControl>
                                                   <FormMessage/>
                                               </FormItem>
                                           )}/>
                                <FormField control={form.control} name={`sessions.${index}.intent`}
                                           render={({field}) => (
                                               <FormItem>
                                                   <FormLabel className="text-xs">Intent</FormLabel>
                                                   <Select onValueChange={field.onChange} value={field.value}>
                                                       <FormControl><SelectTrigger><SelectValue
                                                           placeholder="Select Intent"/></SelectTrigger></FormControl>
                                                       <SelectContent>
                                                           <SelectItem value="Arrival">Arrival</SelectItem>
                                                           <SelectItem value="Departure">Departure</SelectItem>
                                                       </SelectContent>
                                                   </Select>
                                                   <FormMessage/>
                                               </FormItem>
                                           )}/>
                            </div>
                            <FormField control={form.control}
                                       name={`sessions.${index}.targetTime`}
                                       render={({field}) => (
                                           <FormItem>
                                               <FormLabel className="text-xs font-normal">Target Time</FormLabel>
                                               <DateTimePicker field={field}
                                                               disabledDays={[{before: startDate, after: endDate}]}/>
                                               <FormMessage/>
                                           </FormItem>
                                       )}/>
                        </div>
                    ))}
                    <Button type="button" variant="outline" size="sm" className="mt-2" onClick={() => {
                        const baseDate = form.getValues("startDate") || new Date();
                        const newActivityTime = new Date(baseDate);
                        newActivityTime.setHours(9, 0, 0, 0);
                        append({
                            activityName: "",
                            intent: 'Arrival',
                            targetTime: newActivityTime,
                        });
                    }}>
                        <IconPlus className="mr-2 h-4 w-4"/>Add Session
                    </Button>
                    <FormField control={form.control} name="sessions"
                               render={({fieldState}) => (
                                   <FormItem>
                                       <FormMessage>{fieldState.error?.message}</FormMessage>
                                   </FormItem>
                               )}/>
                </div>
            </form>
        </Form>
    );
}
