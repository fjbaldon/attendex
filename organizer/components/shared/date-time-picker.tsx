"use client";

import {Button} from "@/components/ui/button";
import {Calendar} from "@/components/ui/calendar";
import {FormControl} from "@/components/ui/form";
import {Input} from "@/components/ui/input";
import {Popover, PopoverContent, PopoverTrigger} from "@/components/ui/popover";
import {cn} from "@/lib/utils";
import {format} from "date-fns";
import {CalendarIcon, ClockIcon} from "lucide-react";
import {ControllerRenderProps, FieldValues, FieldPath} from "react-hook-form";
import * as React from "react";

type CalendarProps = React.ComponentProps<typeof Calendar>;

interface DateTimePickerProps<
    TFieldValues extends FieldValues = FieldValues,
    TName extends FieldPath<TFieldValues> = FieldPath<TFieldValues>
> {
    field: ControllerRenderProps<TFieldValues, TName>;
    disabledDays?: CalendarProps["disabled"];
}

const formatTimeToInputValue = (date: Date | null | undefined) => {
    if (!date || isNaN(date.getTime())) {
        return "00:00";
    }
    return format(date, "HH:mm");
};

const combineDateAndTime = (date: Date, time: string): Date => {
    const [hours, minutes] = time.split(':');
    const newDate = new Date(date);
    newDate.setHours(parseInt(hours, 10), parseInt(minutes, 10), 0, 0);
    return newDate;
};

export function DateTimePicker<
    TFieldValues extends FieldValues = FieldValues,
    TName extends FieldPath<TFieldValues> = FieldPath<TFieldValues>
>({field, disabledDays}: DateTimePickerProps<TFieldValues, TName>) {
    const [isCalendarOpen, setIsCalendarOpen] = React.useState(false);

    return (
        <div className="flex items-center gap-2">
            <Popover open={isCalendarOpen} onOpenChange={setIsCalendarOpen}>
                <PopoverTrigger asChild>
                    <FormControl>
                        <Button
                            variant={"outline"}
                            className={cn(
                                "justify-start pl-3 text-left font-normal flex-1 min-w-0",
                                !field.value && "text-muted-foreground"
                            )}
                        >
                            <CalendarIcon className="mr-2 h-4 w-4 shrink-0 text-muted-foreground"/>
                            <span>
                                {field.value && !isNaN(new Date(field.value).getTime()) ? (
                                    format(new Date(field.value), "MMM d, yyyy")
                                ) : (
                                    "Pick a date"
                                )}
                            </span>
                        </Button>
                    </FormControl>
                </PopoverTrigger>
                <PopoverContent className="w-auto p-0" align="start">
                    <Calendar
                        mode="single"
                        selected={field.value}
                        onSelect={(date) => {
                            if (!date) return;
                            const time = formatTimeToInputValue(field.value);
                            field.onChange(combineDateAndTime(date, time));
                            setIsCalendarOpen(false);
                        }}
                        fixedWeeks
                        disabled={disabledDays}
                    />
                </PopoverContent>
            </Popover>
            <div className="relative w-32 shrink-0">
                <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                    <ClockIcon className="h-4 w-4 text-muted-foreground"/>
                </div>
                <Input
                    type="time"
                    className="w-full pl-10 appearance-none [&::-webkit-calendar-picker-indicator]:hidden"
                    value={formatTimeToInputValue(field.value)}
                    onChange={(e) => {
                        const currentDate = (field.value && !isNaN(new Date(field.value).getTime())) ? new Date(field.value) : new Date();
                        field.onChange(combineDateAndTime(currentDate, e.target.value));
                    }}
                />
            </div>
        </div>
    );
}
