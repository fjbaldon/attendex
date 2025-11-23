"use client";

import {ColumnDef} from "@tanstack/react-table";
import {Attribute} from "@/types";
import {Badge} from "@/components/ui/badge";
import {IconPencil, IconTrash} from "@tabler/icons-react";
import {createActionsColumn} from "@/components/shared/data-table-action-column";

export const columns: ColumnDef<Attribute>[] = [
    {
        accessorKey: "name",
        header: () => <div className="pl-4">Attribute Name</div>,
        cell: ({row}) => <div className="pl-4 font-medium">{row.original.name}</div>,
    },
    {
        accessorKey: "type",
        header: "Type",
        cell: ({row}) => <Badge variant="secondary">{row.original.type}</Badge>,
    },
    {
        accessorKey: "options",
        header: "Options",
        cell: ({row}) => <div
            className="text-muted-foreground text-sm truncate max-w-xs">{row.original.options?.join(', ') || 'N/A'}</div>,
    },
    createActionsColumn<Attribute>([
        {
            icon: IconPencil,
            label: "Edit Attribute",
            onClick: (row, table) => table.options.meta?.openEditDialog?.(row.original),
        },
        {
            icon: IconTrash,
            label: "Delete Attribute",
            isDestructive: true,
            onClick: (row, table) => table.options.meta?.openDeleteDialog?.(row.original),
        },
    ])
];
