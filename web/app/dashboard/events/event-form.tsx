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

    const form = useForm({
        resolver: zodResolver(eventSchema),
        mode: "onChange",
        defaultValues: event
            ? {
                eventName: event.eventName || "",
                startDate: new Date(event.startDate),
                endDate: new Date(event.endDate),
                onTimeWindowMinutesBefore: event.onTimeWindowMinutesBefore,
                onTimeWindowMinutesAfter: event.onTimeWindowMinutesAfter,
                timeSlots: event.timeSlots.map(ts => ({
                    activityName: ts.activityName,
                    targetTime: new Date(ts.targetTime),
                    type: ts.type
                }))
            }
            : {
                eventName: "",
                startDate: startOfDay(new Date()),
                endDate: endOfDay(new Date()),
                onTimeWindowMinutesBefore: 30,
                onTimeWindowMinutesAfter: 30,
                timeSlots: [],
            },
    });

    useEffect(() => {
        if (event) {
            form.reset({
                eventName: event.eventName || "",
                startDate: new Date(event.startDate),
                endDate: new Date(event.endDate),
                onTimeWindowMinutesBefore: event.onTimeWindowMinutesBefore,
                onTimeWindowMinutesAfter: event.onTimeWindowMinutesAfter,
                timeSlots: event.timeSlots.map(ts => ({
                    activityName: ts.activityName,
                    targetTime: new Date(ts.targetTime),
                    type: ts.type
                }))
            });
        } else {
            const today = new Date();
            form.reset({
                eventName: "",
                startDate: startOfDay(today),
                endDate: endOfDay(today),
                onTimeWindowMinutesBefore: 30,
                onTimeWindowMinutesAfter: 30,
                timeSlots: [],
            });
        }
    }, [event, form]);

    const {fields, append, remove} = useFieldArray({
        control: form.control,
        name: "timeSlots",
    });

    const startDate = form.watch("startDate");
    const endDate = form.watch("endDate");

    React.useEffect(() => {
        if (fields.length > 0 && startDate && endDate) {
            void form.trigger("timeSlots");
        }
    }, [startDate, endDate, form, fields.length]);

    return (
        <Form {...form}>
            <form id="event-form" onSubmit={form.handleSubmit(onSubmit, (errors) => {
                console.log("Form validation errors:", errors);
            })} className="space-y-4">
                <FormField
                    control={form.control} name="eventName" render={({field}) => (
                    <FormItem>
                        <FormLabel>Event Name</FormLabel>
                        <FormControl><Input placeholder="e.g., Annual Tech Conference" {...field}/></FormControl>
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
                    <FormDescription>Define the grace period around an activity&#39;s target time to be considered
                        &#34;On-Time&#34;.</FormDescription>
                    <div className="grid grid-cols-2 gap-4">
                        <FormField control={form.control} name="onTimeWindowMinutesBefore" render={({field}) => (
                            <FormItem>
                                <FormLabel className="text-xs font-normal">Minutes Before Target</FormLabel>
                                <FormControl>
                                    <Input
                                        type="number"
                                        min="0"
                                        {...field}
                                        value={field.value === undefined || field.value === null ? "" : String(field.value)}
                                        onChange={(e) => {
                                            const value = e.target.valueAsNumber;
                                            field.onChange(isNaN(value) ? "" : value);
                                        }}
                                    />
                                </FormControl>
                                <FormMessage/>
                            </FormItem>
                        )}/>
                        <FormField control={form.control} name="onTimeWindowMinutesAfter" render={({field}) => (
                            <FormItem>
                                <FormLabel className="text-xs font-normal">Minutes After Target</FormLabel>
                                <FormControl>
                                    <Input
                                        type="number"
                                        min="0"
                                        {...field}
                                        value={field.value === undefined || field.value === null ? "" : String(field.value)}
                                        onChange={(e) => {
                                            const value = e.target.valueAsNumber;
                                            field.onChange(isNaN(value) ? "" : value);
                                        }}
                                    />
                                </FormControl>
                                <FormMessage/>
                            </FormItem>
                        )}/>
                    </div>
                </div>

                <Separator className="my-4"/>

                <div className="space-y-1">
                    <FormLabel>Event Activities</FormLabel>
                    <p className="text-sm text-muted-foreground">Define key moments for your event like check-ins or
                        exits.</p>
                    {form.formState.errors.timeSlots?.root && (
                        <p className="text-sm font-medium text-destructive mt-2">
                            {form.formState.errors.timeSlots.root.message}
                        </p>
                    )}
                </div>

                <div className="space-y-4">
                    {fields
                        .slice()
                        .sort((a, b) => (a.targetTime || 0) > (b.targetTime || 0) ? 1 : -1)
                        .map((field) => {
                            const originalIndex = fields.findIndex(f => f.id === field.id);
                            return (
                                <div key={field.id} className="rounded-lg border bg-muted/50 p-4 space-y-4 relative">
                                    <Button type="button" variant="ghost" size="icon"
                                            onClick={() => remove(originalIndex)}
                                            className="text-muted-foreground hover:text-destructive absolute top-2 right-2 h-7 w-7">
                                        <IconTrash className="h-4 w-4"/>
                                    </Button>
                                    <div className="space-y-2">
                                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-4">
                                            <FormField control={form.control}
                                                       name={`timeSlots.${originalIndex}.activityName`}
                                                       render={({field}) => (
                                                           <FormItem>
                                                               <FormLabel className="text-xs">Activity Name</FormLabel>
                                                               <FormControl><Input
                                                                   placeholder="e.g., Morning Registration" {...field} /></FormControl>
                                                           </FormItem>
                                                       )}/>
                                            <FormField control={form.control} name={`timeSlots.${originalIndex}.type`}
                                                       render={({field}) => (
                                                           <FormItem>
                                                               <FormLabel className="text-xs">Type</FormLabel>
                                                               <Select onValueChange={field.onChange}
                                                                       value={field.value}>
                                                                   <FormControl><SelectTrigger><SelectValue
                                                                       placeholder="Select Type"/></SelectTrigger></FormControl>
                                                                   <SelectContent>
                                                                       <SelectItem
                                                                           value="CHECK_IN">Check-in</SelectItem>
                                                                       <SelectItem
                                                                           value="CHECK_OUT">Check-out</SelectItem>
                                                                   </SelectContent>
                                                               </Select>
                                                           </FormItem>
                                                       )}/>
                                        </div>
                                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-4">
                                            <FormField control={form.control}
                                                       name={`timeSlots.${originalIndex}.activityName`}
                                                       render={() => (
                                                           <FormItem>
                                                               <FormMessage/>
                                                           </FormItem>
                                                       )}/>
                                            <FormField control={form.control} name={`timeSlots.${originalIndex}.type`}
                                                       render={() => (
                                                           <FormItem>
                                                               <FormMessage/>
                                                           </FormItem>
                                                       )}/>
                                        </div>
                                    </div>
                                    <Separator/>
                                    <div className="space-y-2">
                                        <FormField control={form.control}
                                                   name={`timeSlots.${originalIndex}.targetTime`}
                                                   render={({field}) => {
                                                       const startDate = form.watch("startDate");
                                                       const endDate = form.watch("endDate");

                                                       const disabledDays = [];
                                                       if (startDate) {
                                                           disabledDays.push({before: startOfDay(startDate)});
                                                       }
                                                       if (endDate) {
                                                           disabledDays.push({after: endOfDay(endDate)});
                                                       }

                                                       return (
                                                           <FormItem>
                                                               <FormLabel className="text-xs font-normal">Target
                                                                   Time</FormLabel>
                                                               <DateTimePicker
                                                                   field={field}
                                                                   disabledDays={disabledDays.length > 0 ? disabledDays : undefined}
                                                               />
                                                               <FormMessage/>
                                                           </FormItem>
                                                       );
                                                   }}/>
                                    </div>
                                </div>
                            );
                        })}
                    <Button type="button" variant="outline" size="sm" className="mt-2" onClick={() => {
                        const baseDate = form.getValues("startDate") || new Date();
                        const newActivityTime = new Date(baseDate);
                        newActivityTime.setHours(9, 0, 0, 0);
                        append({
                            activityName: "",
                            type: 'CHECK_IN',
                            targetTime: newActivityTime,
                        });
                    }}>
                        <IconPlus className="mr-2 h-4 w-4"/>Add Activity
                    </Button>
                    <FormField control={form.control} name="timeSlots"
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
