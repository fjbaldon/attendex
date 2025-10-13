"use client";

import {ColumnDef, RowData} from "@tanstack/react-table";
import {AttendeeResponse, CustomFieldDefinition} from "@/types";
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
import {IconDotsVertical} from "@tabler/icons-react";

declare module '@tanstack/react-table' {
    interface TableMeta<TData extends RowData> {
        openEditDialog?: (attendee: TData) => void;
        openDeleteDialog: (attendee: TData) => void;
    }
}

export const getColumns = (customFieldDefs: CustomFieldDefinition[]): ColumnDef<AttendeeResponse>[] => {
    const standardColumns: ColumnDef<AttendeeResponse>[] = [
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
    ];

    const customFieldColumns: ColumnDef<AttendeeResponse>[] = customFieldDefs.map(def => ({
        accessorKey: `customFields.${def.fieldName}`,
        header: def.fieldName,
        cell: ({getValue}) => {
            const value = getValue<unknown>();

            if (value === null || value === undefined || value === '') {
                return <span className="text-muted-foreground text-sm italic">N/A</span>;
            }

            return String(value);
        }
    }));

    const actionColumn: ColumnDef<AttendeeResponse> = {
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
                            <DropdownMenuItem onClick={() => table.options.meta?.openEditDialog?.(attendee)}>
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
    };

    return [...standardColumns, ...customFieldColumns, actionColumn];
};
