"use client";

import {ColumnDef} from "@tanstack/react-table";
import {CustomFieldDefinition} from "@/types";
import {Badge} from "@/components/ui/badge";
import {IconPencil, IconTrash} from "@tabler/icons-react";
import {selectColumn} from "@/components/shared/data-table-columns";
import {createActionsColumn} from "@/components/shared/data-table-action-column";

export const columns: ColumnDef<CustomFieldDefinition>[] = [
    selectColumn<CustomFieldDefinition>(),
    {
        accessorKey: "fieldName",
        header: "Field Name",
        cell: ({row}) => <div className="font-medium">{row.original.fieldName}</div>,
    },
    {
        accessorKey: "fieldType",
        header: "Type",
        cell: ({row}) => <Badge variant="secondary">{row.original.fieldType}</Badge>,
    },
    {
        accessorKey: "options",
        header: "Options",
        cell: ({row}) => <div
            className="text-muted-foreground text-sm truncate max-w-xs">{row.original.options?.join(', ') || 'N/A'}</div>,
    },
    createActionsColumn<CustomFieldDefinition>([
        {
            icon: IconPencil,
            label: "Edit Field",
            onClick: (row, table) => table.options.meta?.openEditDialog?.(row.original),
        },
        {
            icon: IconTrash,
            label: "Delete Field",
            isDestructive: true,
            onClick: (row, table) => table.options.meta?.openDeleteDialog?.(row.original),
        },
    ])
];
