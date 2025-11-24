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
        cell: ({row}) => {
            const options = row.original.options || [];

            if (options.length === 0) return <span className="text-muted-foreground text-sm italic">None</span>;

            // Show first 3 options, then a "+N more" badge if there are many
            const displayOptions = options.slice(0, 3);
            const remainder = options.length - 3;

            return (
                <div className="flex flex-wrap gap-1">
                    {displayOptions.map((opt, i) => (
                        <Badge key={i} variant="secondary" className="font-normal text-xs border bg-muted/50 text-muted-foreground">
                            {opt}
                        </Badge>
                    ))}
                    {remainder > 0 && (
                        <Badge variant="outline" className="text-xs text-muted-foreground">
                            +{remainder}
                        </Badge>
                    )}
                </div>
            );
        },
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
