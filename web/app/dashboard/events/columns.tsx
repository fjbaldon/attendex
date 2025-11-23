"use client";

import {ColumnDef} from "@tanstack/react-table";
import {EventResponse} from "@/types";
import {
    IconCalendarOff,
    IconClock,
    IconExternalLink,
    IconPencil,
    IconPlayerPlayFilled,
    IconTrash
} from "@tabler/icons-react";
import {Badge} from "@/components/ui/badge";
import Link from "next/link";
import {Popover, PopoverContent, PopoverTrigger} from "@/components/ui/popover";
import {cn} from "@/lib/utils";
import {createActionsColumn} from "@/components/shared/data-table-action-column";
import {Button} from "@/components/ui/button";
import {AppRouterInstance} from "next/dist/shared/lib/app-router-context.shared-runtime";

const statusMap: { [key: string]: { text: string; icon: React.ReactNode; className: string } } = {
    ACTIVE: {
        text: "Active",
        icon: <IconPlayerPlayFilled className="h-3 w-3"/>,
        className: "bg-green-100 text-green-800 border-green-200 dark:bg-green-900/40 dark:text-green-400 dark:border-green-800/60"
    },
    ONGOING: {
        text: "Ongoing",
        icon: <IconClock className="h-3 w-3"/>,
        className: "bg-blue-100 text-blue-800 border-blue-200 dark:bg-blue-900/40 dark:text-blue-400 dark:border-blue-800/60"
    },
    UPCOMING: {
        text: "Upcoming",
        icon: <IconClock className="h-3 w-3"/>,
        className: "bg-transparent text-muted-foreground"
    },
    PAST: {
        text: "Past",
        icon: <IconCalendarOff className="h-3 w-3"/>,
        className: "bg-transparent text-muted-foreground"
    },
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

export const getColumns = (router: AppRouterInstance): ColumnDef<EventResponse>[] => [
    {
        accessorKey: "name",
        header: () => <div className="pl-4">Event</div>,
        cell: ({row}) => (
            <div className="pl-4">
                <Link href={`/dashboard/events/${row.original.id}`}
                      className="font-medium text-primary underline-offset-4 hover:underline">
                    {row.original.name}
                </Link>
            </div>
        ),
        enableHiding: false,
    },
    {
        accessorKey: "status",
        header: "Status",
        cell: ({row}) => {
            const status = statusMap[row.original.status] || statusMap.UPCOMING;
            return (
                <Badge variant={"outline"} className={cn("gap-1.5", status.className)}>
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
        id: 'sessions',
        header: 'Sessions',
        cell: ({row}) => {
            const {sessions} = row.original;
            if (!sessions || sessions.length === 0) {
                return <span className="text-muted-foreground italic text-sm">No sessions</span>;
            }

            const summary = `${sessions.length} sessions`;

            return (
                <Popover>
                    <PopoverTrigger asChild>
                        <Button variant="link"
                                className="p-0 h-auto font-normal text-primary text-sm">{summary}</Button>
                    </PopoverTrigger>
                    <PopoverContent className="w-80">
                        <div className="grid gap-4">
                            <div className="space-y-2">
                                <h4 className="font-medium leading-none">Event Sessions</h4>
                                <p className="text-sm text-muted-foreground">
                                    Key scheduled times for this event.
                                </p>
                            </div>
                            <div className="grid gap-2">
                                {sessions.map((session, index) => (
                                    <div key={index}
                                         className="grid grid-cols-[auto_1fr_auto] items-center gap-x-4 gap-y-1 text-sm">
                                        <Badge
                                            variant={session.intent === 'Arrival' ? 'default' : 'secondary'}
                                            className="font-normal"
                                        >
                                            {session.intent}
                                        </Badge>
                                        <span className="font-medium truncate">{session.activityName}</span>
                                        <span className="text-muted-foreground justify-self-end">
                                            {formatTime(session.targetTime)}
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
    createActionsColumn<EventResponse>([
        {
            icon: IconPencil,
            label: "Edit Event",
            onClick: (row, table) => table.options.meta?.openEditDialog?.(row.original),
        },
        {
            icon: IconExternalLink,
            label: "View Details",
            onClick: (row) => {
                router.push(`/dashboard/events/${row.original.id}`);
            },
        },
        {
            icon: IconTrash,
            label: "Delete Event",
            isDestructive: true,
            onClick: (row, table) => table.options.meta?.openDeleteDialog?.(row.original),
        }
    ]),
];
