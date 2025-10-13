"use client";

import {ColumnDef, RowData} from "@tanstack/react-table";
import {OrganizerResponse} from "@/types";
import {Button} from "@/components/ui/button";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {IconDotsVertical} from "@tabler/icons-react";
import {Checkbox} from "@/components/ui/checkbox";

declare module '@tanstack/react-table' {
    interface TableMeta<TData extends RowData> {
        openDeleteDialog: (organizer: TData) => void;
    }
}

export const columns: ColumnDef<OrganizerResponse>[] = [
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
        accessorKey: "email",
        header: "Email Address",
        cell: ({row}) => {
            return <div className="font-medium">{row.original.email}</div>;
        }
    },
    {
        id: "actions",
        cell: ({row, table}) => {
            const organizer = row.original;
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
                            <DropdownMenuItem
                                variant="destructive"
                                onClick={() => table.options.meta?.openDeleteDialog(organizer)}
                            >
                                Remove Organizer
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>
            );
        },
    },
];
