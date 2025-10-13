"use client";

import {ColumnDef, RowData} from "@tanstack/react-table";
import {AttendeeResponse} from "@/types";
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
        openDeleteDialog: (attendee: TData) => void;
    }
}

export const getColumns = (): ColumnDef<AttendeeResponse>[] => [
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
        accessorKey: "uniqueIdentifier",
        header: "Identifier",
    },
    {
        accessorKey: "lastName",
        header: "Last Name",
        cell: ({row}) => <div className="font-medium">{row.original.lastName}</div>,
    },
    {
        accessorKey: "firstName",
        header: "First Name",
        cell: ({row}) => <div className="font-medium">{row.original.firstName}</div>,
    },
    {
        id: "actions",
        cell: ({row, table}) => {
            const attendee = row.original;
            return (
                <div className="flex justify-end">
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button variant="ghost"
                                    className="data-[state=open]:bg-muted text-muted-foreground flex size-8 p-0">
                                <IconDotsVertical/>
                                <span className="sr-only">Open menu</span>
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end" className="w-48">
                            <DropdownMenuLabel>Actions</DropdownMenuLabel>
                            <DropdownMenuItem
                                variant="destructive"
                                onClick={() => table.options.meta?.openDeleteDialog(attendee)}
                            >
                                Remove from Event
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>
            );
        },
    },
];
