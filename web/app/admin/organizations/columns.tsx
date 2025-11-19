"use client";

import {ColumnDef} from "@tanstack/react-table";
import {Organization} from "@/types";
import {Badge} from "@/components/ui/badge";
import {format} from "date-fns";
import {cn} from "@/lib/utils";
import {IconCalendarEvent, IconSettings} from "@tabler/icons-react";
import {createActionsColumn} from "@/components/shared/data-table-action-column";

const statusMap: { [key: string]: { text: string; className: string } } = {
    ACTIVE: {
        text: "Active",
        className: "bg-green-100 text-green-800 border-green-200 dark:bg-green-900/40 dark:text-green-400 dark:border-green-800/60"
    },
    INACTIVE: {
        text: "Inactive",
        className: "bg-gray-100 text-gray-800 border-gray-200 dark:bg-gray-900/40 dark:text-gray-400 dark:border-gray-800/60"
    },
    SUSPENDED: {
        text: "Suspended",
        className: "bg-red-100 text-red-800 border-red-200 dark:bg-red-900/40 dark:text-red-400 dark:border-red-800/60"
    },
};

export const columns: ColumnDef<Organization>[] = [
    {
        accessorKey: "name",
        header: () => <div className="pl-4">Organization</div>,
        cell: ({row}) => <div className="pl-4 font-medium">{row.original.name}</div>,
    },
    {
        accessorKey: "lifecycle",
        header: "Status",
        cell: ({row}) => {
            const statusInfo = statusMap[row.original.lifecycle] || statusMap.INACTIVE;
            return <Badge variant="outline"
                          className={cn("capitalize", statusInfo.className)}>{statusInfo.text}</Badge>;
        },
    },
    {
        accessorKey: "subscriptionType",
        header: "Subscription",
        cell: ({row}) => <div className="capitalize">{row.original.subscriptionType?.toLowerCase()}</div>,
    },
    {
        accessorKey: "subscriptionExpiresAt",
        header: "Expires At",
        cell: ({row}) => {
            const expiresAt = row.original.subscriptionExpiresAt;
            return expiresAt ? format(new Date(expiresAt), "PPP") : <span className="text-muted-foreground">—</span>;
        },
    },
    createActionsColumn<Organization>([
        {
            icon: IconSettings,
            label: "Update Status",
            onClick: (row, table) => table.options.meta?.openStatusDialog?.(row.original),
        },
        {
            icon: IconCalendarEvent,
            label: "Update Subscription",
            onClick: (row, table) => table.options.meta?.openSubscriptionDialog?.(row.original),
        },
    ]),
];
