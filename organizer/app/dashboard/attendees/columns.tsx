"use client";

import {ColumnDef, RowData} from "@tanstack/react-table";
import {AttendeeResponse} from "@/types";
import {Button} from "@/components/ui/button";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {Checkbox} from "@/components/ui/checkbox";
import {IconDotsVertical, IconInfoCircle} from "@tabler/icons-react";
import {Popover, PopoverContent, PopoverTrigger} from "@/components/ui/popover";

declare module '@tanstack/react-table' {
    interface TableMeta<TData extends RowData> {
        openEditDialog: (attendee: TData) => void;
        openDeleteDialog: (attendee: TData) => void;
    }
}

export const columns: ColumnDef<AttendeeResponse>[] = [
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
    },
    {
        accessorKey: "firstName",
        header: "First Name",
    },
    {
        accessorKey: "customFields",
        header: "Custom Fields",
        cell: ({row}) => {
            const customFields = row.original.customFields;
            if (!customFields || Object.keys(customFields).length === 0) {
                return <span className="text-muted-foreground">None</span>;
            }
            return (
                <Popover>
                    <PopoverTrigger asChild>
                        <Button variant="ghost" size="sm" className="h-8">
                            <IconInfoCircle className="h-4 w-4 mr-2"/>
                            View Fields
                        </Button>
                    </PopoverTrigger>
                    <PopoverContent className="w-80">
                        <div className="grid gap-4">
                            <div className="space-y-2">
                                <h4 className="font-medium leading-none">Custom Fields</h4>
                                <p className="text-sm text-muted-foreground">
                                    Additional data for this attendee.
                                </p>
                            </div>
                            <pre className="mt-2 w-full rounded-md bg-slate-950 p-4 overflow-x-auto">
                                <code className="text-white">
                                    {JSON.stringify(customFields, null, 2)}
                                </code>
                            </pre>
                        </div>
                    </PopoverContent>
                </Popover>
            )
        }
    },
    {
        id: "actions",
        cell: ({row, table}) => {
            const attendee = row.original;
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
                            <DropdownMenuItem onClick={() => table.options.meta?.openEditDialog(attendee)}>
                                Edit Attendee
                            </DropdownMenuItem>
                            <DropdownMenuSeparator/>
                            <DropdownMenuItem
                                variant="destructive"
                                onClick={() => table.options.meta?.openDeleteDialog(attendee)}
                            >
                                Delete Attendee
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>
            );
        },
    },
];
