"use client";

import {useFieldArray, useForm} from "react-hook-form";
import {z} from "zod";
import {zodResolver} from "@hookform/resolvers/zod";
import {eventSchema} from "@/lib/schemas";
import {EventResponse} from "@/types";
import {Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage} from "@/components/ui/form";
import {Input} from "@/components/ui/input";
import {Button} from "@/components/ui/button";
import {IconCalendar, IconClock, IconPlus, IconTrash} from "@tabler/icons-react";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Separator} from "@/components/ui/separator";
import {Popover, PopoverContent, PopoverTrigger} from "@/components/ui/popover";
import {cn} from "@/lib/utils";
import {endOfDay, format, startOfDay} from "date-fns";
import * as React from "react";
import {useEffect} from "react";
import {Calendar} from "@/components/ui/calendar";
import {DateTimePicker} from "@/components/shared/date-time-picker";
import {Accordion, AccordionContent, AccordionItem, AccordionTrigger} from "@/components/ui/accordion";
import {Badge} from "@/components/ui/badge";

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

    const [accordionValue, setAccordionValue] = React.useState<string | undefined>(undefined);

    React.useEffect(() => {
        if (fields.length > 0 && startDate && endDate) {
            void form.trigger("sessions");
        }
    }, [startDate, endDate, form, fields.length]);

    const handleAddSession = () => {
        const baseDate = form.getValues("startDate") || new Date();
        const newActivityTime = new Date(baseDate);
        newActivityTime.setHours(9, 0, 0, 0);
        append({
            activityName: "",
            intent: 'Arrival',
            targetTime: newActivityTime,
        });
        setTimeout(() => setAccordionValue(`item-${fields.length}`), 100);
    };

    return (
        <Form {...form}>
            <form id="event-form" onSubmit={form.handleSubmit(onSubmit)} className="space-y-6 px-1 pb-4">

                <div className="space-y-4">
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
                </div>

                <div className="space-y-2 rounded-lg border p-4 bg-muted/20">
                    <FormLabel className="text-base">On-Time Window</FormLabel>
                    <FormDescription>Define the grace period (minutes) for Punctuality.</FormDescription>
                    <div className="grid grid-cols-2 gap-4 mt-2">
                        <FormField control={form.control} name="graceMinutesBefore" render={({field}) => (
                            <FormItem>
                                <FormLabel className="text-xs font-normal text-muted-foreground">Before Target</FormLabel>
                                <FormControl>
                                    <Input type="number" min="0" {...field} value={field.value as string}/>
                                </FormControl>
                                <FormMessage/>
                            </FormItem>
                        )}/>
                        <FormField control={form.control} name="graceMinutesAfter" render={({field}) => (
                            <FormItem>
                                <FormLabel className="text-xs font-normal text-muted-foreground">After Target</FormLabel>
                                <FormControl>
                                    <Input type="number" min="0" {...field} value={field.value as string}/>
                                </FormControl>
                                <FormMessage/>
                            </FormItem>
                        )}/>
                    </div>
                </div>

                <Separator />

                <div className="space-y-2">
                    <div className="flex items-center justify-between">
                        <FormLabel className="text-base">Session Timeline</FormLabel>
                        <Button type="button" variant="outline" size="sm" onClick={handleAddSession}>
                            <IconPlus className="mr-2 h-4 w-4"/>Add Session
                        </Button>
                    </div>

                    {form.formState.errors.sessions?.root && (
                        <p className="text-sm font-medium text-destructive">
                            {form.formState.errors.sessions.root.message}
                        </p>
                    )}

                    <div className="relative">
                        {fields.length > 0 && (
                            <div className="absolute left-[15px] top-4 bottom-12 w-px bg-border" />
                        )}

                        <Accordion type="single" collapsible className="space-y-4" value={accordionValue} onValueChange={setAccordionValue}>
                            {fields.map((field, index) => {
                                const currentValues = form.watch(`sessions.${index}`);
                                const timeDisplay = currentValues.targetTime ? format(currentValues.targetTime, "h:mm a") : "--:--";
                                const titleDisplay = currentValues.activityName || "New Session";

                                return (
                                    <AccordionItem key={field.id} value={`item-${index}`} className="border rounded-md bg-card relative z-10 shadow-sm">
                                        <AccordionTrigger className="px-4 py-3 hover:no-underline">
                                            <div className="flex items-center gap-3 w-full text-left">
                                                <Badge variant={currentValues.intent === 'Arrival' ? 'default' : 'secondary'} className="text-[10px] uppercase">
                                                    {currentValues.intent === 'Arrival' ? 'IN' : 'OUT'}
                                                </Badge>
                                                <div className="flex flex-col flex-1">
                                                    <span className="text-sm font-medium">{titleDisplay}</span>
                                                    <span className="text-xs text-muted-foreground flex items-center gap-1">
                                                        <IconClock className="w-3 h-3" /> {timeDisplay}
                                                    </span>
                                                </div>
                                            </div>
                                        </AccordionTrigger>

                                        <AccordionContent className="px-5 pb-5 pt-0">
                                            <div className="h-px bg-border/50 my-2" />

                                            {/* FIXED: Responsive Grid logic */}
                                            {/* Mobile: grid-cols-1 (Stack everything) */}
                                            {/* Desktop (sm+): grid-cols-[1fr_180px] (Side by side) */}
                                            <div className="grid grid-cols-1 sm:grid-cols-[1fr_180px] gap-4 sm:gap-6 items-start">

                                                <FormField
                                                    control={form.control}
                                                    name={`sessions.${index}.activityName`}
                                                    render={({field}) => (
                                                        <FormItem className="w-full">
                                                            <FormLabel className="text-xs">Activity Name</FormLabel>
                                                            <FormControl>
                                                                <Input placeholder="e.g., Keynote" {...field} />
                                                            </FormControl>
                                                            <FormMessage/>
                                                        </FormItem>
                                                    )}
                                                />

                                                <FormField
                                                    control={form.control}
                                                    name={`sessions.${index}.intent`}
                                                    render={({field}) => (
                                                        <FormItem className="w-full">
                                                            <FormLabel className="text-xs">Intent</FormLabel>
                                                            <Select onValueChange={field.onChange} value={field.value}>
                                                                <FormControl>
                                                                    <SelectTrigger className="w-full">
                                                                        <SelectValue/>
                                                                    </SelectTrigger>
                                                                </FormControl>
                                                                <SelectContent>
                                                                    <SelectItem value="Arrival">Arrival</SelectItem>
                                                                    <SelectItem value="Departure">Departure</SelectItem>
                                                                </SelectContent>
                                                            </Select>
                                                            <FormMessage/>
                                                        </FormItem>
                                                    )}
                                                />

                                                <FormField
                                                    control={form.control}
                                                    name={`sessions.${index}.targetTime`}
                                                    render={({field}) => (
                                                        <FormItem className="w-full">
                                                            <FormLabel className="text-xs">Target Time</FormLabel>
                                                            <DateTimePicker field={field} disabledDays={[{before: startDate, after: endDate}]}/>
                                                            <FormMessage/>
                                                        </FormItem>
                                                    )}
                                                />

                                                <FormItem className="w-full">
                                                    {/* Label hidden on desktop for alignment, visible on mobile (or handle spacing) */}
                                                    {/* Actually, using 'invisible' keeps the spacing consistent on desktop. On mobile we can hide it to save space or keep it. */}
                                                    {/* Let's hide it on mobile to save space, and keep invisible on desktop for alignment */}
                                                    <FormLabel className="text-xs hidden sm:block invisible">Remove</FormLabel>
                                                    <FormControl>
                                                        <Button
                                                            type="button"
                                                            variant="destructive"
                                                            size="default"
                                                            className="w-full"
                                                            onClick={() => remove(index)}
                                                        >
                                                            <IconTrash className="mr-2 h-4 w-4"/> Remove
                                                        </Button>
                                                    </FormControl>
                                                </FormItem>
                                            </div>
                                        </AccordionContent>
                                    </AccordionItem>
                                );
                            })}
                        </Accordion>

                        {fields.length === 0 && (
                            <div className="text-center py-8 border-2 border-dashed rounded-lg bg-muted/20">
                                <p className="text-sm text-muted-foreground mb-2">No sessions defined yet.</p>
                                <Button type="button" variant="outline" size="sm" onClick={handleAddSession}>Create your first session</Button>
                            </div>
                        )}
                    </div>
                </div>
            </form>
        </Form>
    );
}
