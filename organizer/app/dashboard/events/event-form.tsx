"use client";

import {useFieldArray, useForm, FieldErrors} from "react-hook-form";
import {z} from "zod";
import {zodResolver} from "@hookform/resolvers/zod";
import {eventSchema} from "@/lib/schemas";
import {EventResponse, TimeSlotType} from "@/types";
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage} from "@/components/ui/form";
import {Input} from "@/components/ui/input";
import {Button} from "@/components/ui/button";
import {DateTimePicker} from "@/components/shared/date-time-picker";
import {IconCalendar, IconPlus, IconTrash} from "@tabler/icons-react";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Separator} from "@/components/ui/separator";
import {Calendar} from "@/components/ui/calendar";
import {Popover, PopoverContent, PopoverTrigger} from "@/components/ui/popover";
import {cn} from "@/lib/utils";
import {format} from "date-fns";
import {toast} from "sonner";

interface EventFormProps {
    event?: EventResponse | null;
    onSubmit: (values: z.infer<typeof eventSchema>) => void;
    isLoading: boolean;
}

export function EventForm({event, onSubmit}: EventFormProps) {
    const form = useForm<z.infer<typeof eventSchema>>({
        resolver: zodResolver(eventSchema),
        defaultValues: {
            eventName: event?.eventName || "",
            startDate: event ? new Date(event.startDate) : undefined,
            endDate: event ? new Date(event.endDate) : undefined,
            timeSlots: event?.timeSlots?.map(ts => ({
                ...ts,
                startTime: new Date(ts.startTime),
                endTime: new Date(ts.endTime),
            })) || [],
        },
    });

    const {fields, append, remove} = useFieldArray({
        control: form.control,
        name: "timeSlots",
    });

    const eventStartDate = form.watch("startDate");

    const onInvalid = (errors: FieldErrors<z.infer<typeof eventSchema>>) => {
        function findFirstErrorMessage(errorObject: any): string | null {
            if (!errorObject) return null;

            if (typeof errorObject.message === 'string') {
                return errorObject.message;
            }

            for (const key in errorObject) {
                if (Object.prototype.hasOwnProperty.call(errorObject, key)) {
                    const nestedError = errorObject[key];
                    if (typeof nestedError === 'object' && nestedError !== null) {
                        const message = findFirstErrorMessage(nestedError);
                        if (message) {
                            return message;
                        }
                    }
                }
            }
            return null;
        }

        const errorMessage = findFirstErrorMessage(errors) || "Please check the form for errors and try again.";
        toast.error("Validation Failed", {description: errorMessage});
    };

    return (
        <Form {...form}>
            <form id="event-form" onSubmit={form.handleSubmit(onSubmit, onInvalid)} className="space-y-4">
                <FormField
                    control={form.control}
                    name="eventName"
                    render={({field}) => (
                        <FormItem>
                            <FormLabel>Event Name</FormLabel>
                            <FormControl>
                                <Input placeholder="e.g., Annual Tech Conference" {...field} />
                            </FormControl>
                            <FormMessage/>
                        </FormItem>
                    )}
                />
                <div className="grid grid-cols-2 gap-4">
                    <FormField
                        control={form.control}
                        name="startDate"
                        render={({field}) => (
                            <FormItem className="flex flex-col">
                                <FormLabel>Event Start Date</FormLabel>
                                <Popover>
                                    <PopoverTrigger asChild>
                                        <FormControl>
                                            <Button variant={"outline"}
                                                    className={cn("pl-3 text-left font-normal", !field.value && "text-muted-foreground")}>
                                                {field.value ? (format(field.value, "PPP")) : (
                                                    <span>Pick a date</span>)}
                                                <IconCalendar className="ml-auto h-4 w-4 opacity-50"/>
                                            </Button>
                                        </FormControl>
                                    </PopoverTrigger>
                                    <PopoverContent className="w-auto p-0" align="start">
                                        <Calendar mode="single" selected={field.value} onSelect={field.onChange}
                                                  initialFocus/>
                                    </PopoverContent>
                                </Popover>
                                <FormMessage/>
                            </FormItem>
                        )}
                    />
                    <FormField
                        control={form.control}
                        name="endDate"
                        render={({field}) => (
                            <FormItem className="flex flex-col">
                                <FormLabel>Event End Date</FormLabel>
                                <Popover>
                                    <PopoverTrigger asChild>
                                        <FormControl>
                                            <Button variant={"outline"}
                                                    className={cn("pl-3 text-left font-normal", !field.value && "text-muted-foreground")}>
                                                {field.value ? (format(field.value, "PPP")) : (
                                                    <span>Pick a date</span>)}
                                                <IconCalendar className="ml-auto h-4 w-4 opacity-50"/>
                                            </Button>
                                        </FormControl>
                                    </PopoverTrigger>
                                    <PopoverContent className="w-auto p-0" align="start">
                                        <Calendar mode="single" selected={field.value} onSelect={field.onChange}
                                                  disabled={{before: form.watch("startDate")}} initialFocus/>
                                    </PopoverContent>
                                </Popover>
                                <FormMessage/>
                            </FormItem>
                        )}
                    />
                </div>
                <Separator className="my-4"/>
                <div>
                    <FormLabel>Event Schedule</FormLabel>
                    <p className="text-sm text-muted-foreground">Define specific check-in and check-out periods.</p>
                </div>

                <div className="space-y-4">
                    {fields.map((item, index) => (
                        <div key={item.id} className="rounded-lg border bg-muted/50 p-4 space-y-4">
                            <div className="flex justify-between items-center">
                                <FormField
                                    control={form.control}
                                    name={`timeSlots.${index}.type`}
                                    render={({field}) => (
                                        <FormItem className="w-40">
                                            <Select onValueChange={field.onChange} value={field.value}>
                                                <FormControl>
                                                    <SelectTrigger>
                                                        <SelectValue placeholder="Type"/>
                                                    </SelectTrigger>
                                                </FormControl>
                                                <SelectContent>
                                                    <SelectItem value="CHECK_IN">Check-in</SelectItem>
                                                    <SelectItem value="CHECK_OUT">Check-out</SelectItem>
                                                </SelectContent>
                                            </Select>
                                            <FormMessage/>
                                        </FormItem>
                                    )}
                                />
                                <Button
                                    type="button"
                                    variant="ghost"
                                    size="icon"
                                    onClick={() => remove(index)}
                                    className="text-muted-foreground hover:text-destructive"
                                >
                                    <IconTrash className="h-4 w-4"/>
                                </Button>
                            </div>
                            <div className="space-y-4">
                                <FormField
                                    control={form.control}
                                    name={`timeSlots.${index}.startTime`}
                                    render={({field}) => (
                                        <FormItem>
                                            <FormLabel className="text-xs font-normal">Start Time</FormLabel>
                                            <DateTimePicker field={field}/>
                                            <FormMessage/>
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name={`timeSlots.${index}.endTime`}
                                    render={({field}) => (
                                        <FormItem>
                                            <FormLabel className="text-xs font-normal">End Time</FormLabel>
                                            <DateTimePicker field={field}/>
                                            <FormMessage/>
                                        </FormItem>
                                    )}
                                />
                            </div>
                        </div>
                    ))}
                    <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        className="mt-2"
                        onClick={() => {
                            const baseDate = eventStartDate || new Date();
                            const defaultStartTime = new Date(baseDate);
                            defaultStartTime.setHours(8, 0, 0, 0);

                            const defaultEndTime = new Date(baseDate);
                            defaultEndTime.setHours(8, 30, 0, 0);

                            append({
                                type: 'CHECK_IN' as TimeSlotType,
                                startTime: defaultStartTime,
                                endTime: defaultEndTime,
                            });
                        }}
                    >
                        <IconPlus className="mr-2 h-4 w-4"/>
                        Add Time Slot
                    </Button>
                    <FormField
                        control={form.control}
                        name="timeSlots"
                        render={() => (<FormMessage className="pt-2"/>)}
                    />
                </div>
            </form>
        </Form>
    );
}
