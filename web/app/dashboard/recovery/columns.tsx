"use client";

import {ColumnDef} from "@tanstack/react-table";
import {OrphanedEntry} from "@/types";
import {format} from "date-fns";
import {Badge} from "@/components/ui/badge";
import {IconTrash} from "@tabler/icons-react";
import {createActionsColumn} from "@/components/shared/data-table-action-column";
import {selectColumn} from "@/components/shared/data-table-columns";

export const columns: ColumnDef<OrphanedEntry>[] = [
    selectColumn<OrphanedEntry>(),
    {
        accessorKey: "originalEventId",
        header: "Target Event ID",
        cell: ({row}) => <div className="font-mono text-xs">{row.original.originalEventId}</div>,
    },
    {
        id: "attendee",
        header: "Raw Attendee Data",
        cell: ({row}) => {
            try {
                const payload = JSON.parse(row.original.rawPayload);
                return (
                    <div className="flex flex-col">
                        <span className="font-medium">{payload.snapshotFirstName} {payload.snapshotLastName}</span>
                        <span className="text-xs text-muted-foreground">ID: {payload.snapshotIdentity}</span>
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
            icon: IconTrash,
            label: "Dismiss",
            isDestructive: true,
            onClick: (row, table) => table.options.meta?.openDeleteDialog?.(row.original),
        }
    ]),
];
