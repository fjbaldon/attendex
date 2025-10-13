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

declare module '@tanstack/react-table' {
    interface TableMeta<TData extends RowData> {
        openEditDialog?: (event: TData) => void;
        openDeleteDialog: (event: TData) => void;
    }
}

const getEventStatus = (startDateStr: string, endDateStr: string): { text: string; icon: React.ReactNode } => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const startDate = new Date(startDateStr);
    const endDate = new Date(endDateStr);

    if (endDate < today) {
        return {text: "Past", icon: <IconCalendarOff className="mr-1 h-3.5 w-3.5"/>};
    } else if (startDate > today) {
        return {text: "Upcoming", icon: <IconLoader className="mr-1 h-3.5 w-3.5 animate-spin"/>};
    } else {
        return {
            text: "Ongoing",
            icon: <IconCircleCheckFilled
                className="mr-1 h-3.5 w-3.5 fill-green-500 text-green-500 dark:fill-green-400 dark:text-green-400"/>
        };
    }
};

const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
    });
};

const formatTime = (dateString: string) => {
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
            const status = getEventStatus(row.original.startDate, row.original.endDate);
            return (
                <Badge variant="outline" className="text-muted-foreground">
                    {status.icon}
                    {status.text}
                </Badge>
            );
        },
    },
    {
        accessorKey: "startDate",
        header: "Start",
        cell: ({row}) => {
            const date = formatDate(row.original.startDate);
            const time = formatTime(row.original.startDate);
            return (
                <div className="flex flex-col">
                    <span>{date}</span>
                    <span className="text-xs text-muted-foreground">{time}</span>
                </div>
            );
        },
    },
    {
        accessorKey: "endDate",
        header: "End",
        cell: ({row}) => {
            const date = formatDate(row.original.endDate);
            const time = formatTime(row.original.endDate);
            return (
                <div className="flex flex-col">
                    <span>{date}</span>
                    <span className="text-xs text-muted-foreground">{time}</span>
                </div>
            );
        },
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
                                <Link href={`/dashboard/events/${event.id}`}>View Roster</Link>
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
