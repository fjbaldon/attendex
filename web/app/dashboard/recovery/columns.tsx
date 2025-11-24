"use client";

import {ColumnDef} from "@tanstack/react-table";
import {OrphanedEntry} from "@/types";
import {format} from "date-fns";
import {Badge} from "@/components/ui/badge";
import {IconRestore, IconTrash} from "@tabler/icons-react";
import {createActionsColumn} from "@/components/shared/data-table-action-column";
import {selectColumn} from "@/components/shared/data-table-columns";

interface OrphanedPayload {
    snapshotFirstName?: string;
    snapshotLastName?: string;
    snapshotIdentity?: string;
    [key: string]: unknown;
}

export const columns: ColumnDef<OrphanedEntry>[] = [
    selectColumn<OrphanedEntry>(),
    {
        id: "eventInfo",
        header: "Target Event",
        cell: ({row}) => (
            <div className="flex flex-col">
                <span className="font-medium">{row.original.originalEventName}</span>
                <span className="text-xs text-muted-foreground font-mono">ID: {row.original.originalEventId}</span>
            </div>
        ),
    },
    {
        id: "attendee",
        header: "Raw Attendee Data",
        cell: ({row}) => {
            try {
                const raw = row.original.rawPayload;
                const payload = (typeof raw === 'string' ? JSON.parse(raw) : raw) as OrphanedPayload;

                return (
                    <div className="flex flex-col">
                        <span className="font-medium">
                            {payload.snapshotFirstName || "?"} {payload.snapshotLastName || "?"}
                        </span>
                        <span className="text-xs text-muted-foreground">
                            ID: {payload.snapshotIdentity || "Unknown"}
                        </span>
                    </div>
                );
            } catch {
                return <span className="text-destructive italic">Corrupt Payload</span>;
            }
        }
    },
    {
        accessorKey: "failureReason",
        header: "Reason",
        cell: ({row}) => (
            <Badge variant="outline" className="text-red-600 border-red-200 bg-red-50">
                {row.original.failureReason}
            </Badge>
        ),
    },
    {
        accessorKey: "createdAt",
        header: "Captured At",
        cell: ({row}) => (
            <div className="text-sm text-muted-foreground">
                {format(new Date(row.original.createdAt), "MMM d, yyyy h:mm a")}
            </div>
        ),
    },
    createActionsColumn<OrphanedEntry>([
        {
            icon: IconRestore,
            label: "Recover",
            onClick: (row, table) => table.options.meta?.openRecoverDialog?.(row.original),
        },
        {
            icon: IconTrash,
            label: "Dismiss",
            isDestructive: true,
            onClick: (row, table) => table.options.meta?.openDeleteDialog?.(row.original),
        }
    ]),
];
