"use client";

import {ColumnDef, RowData} from "@tanstack/react-table";
import {EventResponse} from "@/types";
import {Button} from "@/components/ui/button";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {Checkbox} from "@/components/ui/checkbox";
import {IconDotsVertical, IconCircleCheckFilled, IconLoader, IconCalendarOff} from "@tabler/icons-react";
import {Badge} from "@/components/ui/badge";
import Link from "next/link";
import {Popover, PopoverContent, PopoverTrigger} from "@/components/ui/popover";

declare module '@tanstack/react-table' {
    interface TableMeta<TData extends RowData> {
        openEditDialog?: (event: TData) => void;
        openDeleteDialog: (event: TData) => void;
    }
}

const getEventStatus = (timeSlots: EventResponse['timeSlots']): { text: string; icon: React.ReactNode } => {
    const now = new Date();
    let isUpcoming = true;
    let isPast = true;

    if (!timeSlots || timeSlots.length === 0) {
        return {text: "Unscheduled", icon: <IconCalendarOff className="mr-1 h-3.5 w-3.5"/>};
    }

    for (const slot of timeSlots) {
        const startTime = new Date(slot.startTime);
        const endTime = new Date(slot.endTime);

        if (now >= startTime && now <= endTime) {
            return {
                text: "Ongoing",
                icon: <IconCircleCheckFilled
                    className="mr-1 h-3.5 w-3.5 fill-green-500 text-green-500 dark:fill-green-400 dark:text-green-400"/>
            };
        }
        if (endTime > now) {
            isPast = false;
        }
        if (startTime < now) {
            isUpcoming = false;
        }
    }

    if (isPast) {
        return {text: "Past", icon: <IconCalendarOff className="mr-1 h-3.5 w-3.5"/>};
    }
    if (isUpcoming) {
        return {text: "Upcoming", icon: <IconLoader className="mr-1 h-3.5 w-3.5 animate-spin"/>};
    }
    return {text: "Mixed", icon: <IconCalendarOff className="mr-1 h-3.5 w-3.5"/>};
};


const formatDateRange = (startDateStr: string, endDateStr: string) => {
    if (!startDateStr || !endDateStr) return "N/A";
    const startDate = new Date(startDateStr);
    const endDate = new Date(endDateStr);
    const startFormatted = startDate.toLocaleDateString('en-US', {month: 'short', day: 'numeric'});
    const endFormatted = endDate.toLocaleDateString('en-US', {month: 'short', day: 'numeric', year: 'numeric'});

    if (startDate.getFullYear() !== endDate.getFullYear()) {
        return `${startDate.toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric'
        })} - ${endFormatted}`;
    }

    if (startDate.getMonth() === endDate.getMonth() && startDate.getDate() === endDate.getDate()) {
        return endDate.toLocaleDateString('en-US', {month: 'short', day: 'numeric', year: 'numeric'});
    }

    return `${startFormatted} - ${endFormatted}`;
};


const formatTime = (dateString: string | Date) => {
    return new Date(dateString).toLocaleTimeString('en-US', {
        hour: 'numeric',
        minute: '2-digit',
        hour12: true,
    });
};

export const columns: ColumnDef<EventResponse>[] = [
    {
        id: "select",
        header: ({table}) => (
            <Checkbox
                checked={table.getIsAllPageRowsSelected() || (table.getIsSomePageRowsSelected() && "indeterminate")}
                onCheckedChange={(value) => table.toggleAllPageRowsSelected(!!value)}
                aria-label="Select all"
            />
        ),
        cell: ({row}) => (
            <Checkbox
                checked={row.getIsSelected()}
                onCheckedChange={(value) => row.toggleSelected(!!value)}
                aria-label="Select row"
            />
        ),
        enableSorting: false,
        enableHiding: false,
    },
    {
        accessorKey: "eventName",
        header: "Event",
        cell: ({row}) => (
            <Link href={`/dashboard/events/${row.original.id}`}
                  className="font-medium text-primary underline-offset-4 hover:underline">
                {row.original.eventName}
            </Link>
        ),
        enableHiding: false,
    },
    {
        id: "status",
        header: "Status",
        cell: ({row}) => {
            const status = getEventStatus(row.original.timeSlots);
            return (
                <Badge variant="outline" className="text-muted-foreground">
                    {status.icon}
                    {status.text}
                </Badge>
            );
        },
    },
    {
        id: 'dateRange',
        header: 'Dates',
        cell: ({row}) => {
            const {startDate, endDate} = row.original;
            return <span>{formatDateRange(startDate, endDate)}</span>
        }
    },
    {
        id: 'schedule',
        header: 'Schedule',
        cell: ({row}) => {
            const {timeSlots} = row.original;
            if (!timeSlots || timeSlots.length === 0) {
                return <span className="text-muted-foreground italic text-sm">No schedule</span>;
            }

            const summary = `${timeSlots.length} time slot(s)`;

            return (
                <Popover>
                    <PopoverTrigger asChild>
                        <Button variant="link"
                                className="p-0 h-auto font-normal text-primary text-sm">{summary}</Button>
                    </PopoverTrigger>
                    <PopoverContent className="w-80">
                        <div className="grid gap-4">
                            <div className="space-y-2">
                                <h4 className="font-medium leading-none">Schedule Details</h4>
                                <p className="text-sm text-muted-foreground">
                                    Defined check-in/out periods for this event.
                                </p>
                            </div>
                            <div className="grid gap-2">
                                {timeSlots.map((slot, index) => (
                                    <div key={index}
                                         className="grid grid-cols-[auto_1fr] items-center gap-x-4 gap-y-1 text-sm">
                                        <Badge
                                            variant={slot.type === 'CHECK_IN' ? 'default' : 'secondary'}
                                            className="font-normal"
                                        >
                                            {slot.type === 'CHECK_IN' ? 'Check-in' : 'Check-out'}
                                        </Badge>
                                        <span className="text-muted-foreground">
                                            {formatTime(slot.startTime)} - {formatTime(slot.endTime)}
                                        </span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </PopoverContent>
                </Popover>
            )
        }
    },
    {
        id: "actions",
        cell: ({row, table}) => {
            const event = row.original;
            return (
                <div className="flex justify-end">
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button
                                variant="ghost"
                                className="data-[state=open]:bg-muted text-muted-foreground flex size-8 p-0"
                            >
                                <IconDotsVertical/>
                                <span className="sr-only">Open menu</span>
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end" className="w-40">
                            <DropdownMenuLabel>Actions</DropdownMenuLabel>
                            <DropdownMenuItem onClick={() => table.options.meta?.openEditDialog?.(event)}>
                                Edit Event
                            </DropdownMenuItem>
                            <DropdownMenuItem asChild>
                                <Link href={`/dashboard/events/${event.id}`}>View Details</Link>
                            </DropdownMenuItem>
                            <DropdownMenuSeparator/>
                            <DropdownMenuItem
                                variant="destructive"
                                onClick={() => table.options.meta?.openDeleteDialog(event)}
                            >
                                Delete Event
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>
            );
        },
    },
];
