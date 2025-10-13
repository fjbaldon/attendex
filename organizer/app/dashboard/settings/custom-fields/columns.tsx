"use client";

import {ColumnDef, RowData} from "@tanstack/react-table";
import {CustomFieldDefinition} from "@/types";
import {Checkbox} from "@/components/ui/checkbox";
import {Badge} from "@/components/ui/badge";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger
} from "@/components/ui/dropdown-menu";
import {Button} from "@/components/ui/button";
import {IconDotsVertical} from "@tabler/icons-react";

declare module '@tanstack/react-table' {
    interface TableMeta<TData extends RowData> {
        openEditDialog?: (field: TData) => void;
        openDeleteDialog: (field: TData) => void;
    }
}

export const columns: ColumnDef<CustomFieldDefinition>[] = [
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
    {
        id: "actions",
        cell: ({row, table}) => {
            const field = row.original;
            return (
                <div className="flex justify-end">
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button variant="ghost" className="h-8 w-8 p-0">
                                <span className="sr-only">Open menu</span>
                                <IconDotsVertical className="h-4 w-4"/>
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                            <DropdownMenuLabel>Actions</DropdownMenuLabel>
                            <DropdownMenuItem onClick={() => table.options.meta?.openEditDialog?.(field)}>
                                Edit
                            </DropdownMenuItem>
                            <DropdownMenuSeparator/>
                            <DropdownMenuItem
                                variant="destructive"
                                onClick={() => table.options.meta?.openDeleteDialog(field)}
                            >
                                Delete
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>
            );
        },
    },
];
