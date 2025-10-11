"use client";

import {ColumnDef, RowData} from "@tanstack/react-table";
import {OrganizerResponse} from "@/types";
import {Button} from "@/components/ui/button";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {IconDotsVertical} from "@tabler/icons-react";
import {Badge} from "@/components/ui/badge";
import {Checkbox} from "@/components/ui/checkbox";

declare module '@tanstack/react-table' {
    interface TableMeta<TData extends RowData> {
        openEditDialog: (user: TData) => void;
        openDeleteDialog: (user: TData) => void;
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
        accessorKey: "roleName",
        header: "Role",
        cell: ({row}) => {
            const isOwner = row.original.roleName.toLowerCase() === 'admin';
            return (
                <Badge variant={isOwner ? "default" : "secondary"}>
                    {row.original.roleName}
                </Badge>
            );
        }
    },
    {
        id: "actions",
        cell: ({row, table}) => {
            const user = row.original;
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
                            <DropdownMenuItem onClick={() => table.options.meta?.openEditDialog(user)}>
                                Change Role
                            </DropdownMenuItem>
                            <DropdownMenuSeparator/>
                            <DropdownMenuItem
                                variant="destructive"
                                onClick={() => table.options.meta?.openDeleteDialog(user)}
                            >
                                Remove User
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>
            );
        },
    },
];
