"use client";

import {ColumnDef} from "@tanstack/react-table";
import {SystemAdmin} from "@/types";
import {IconKey, IconTrash} from "@tabler/icons-react";
import {createActionsColumn} from "@/components/shared/data-table-action-column";
import {format} from "date-fns";

export const columns: ColumnDef<SystemAdmin>[] = [
    {
        accessorKey: "email",
        header: () => <div className="pl-4">Email Address</div>,
        cell: ({row}) => <div className="pl-4 font-medium">{row.original.email}</div>,
    },
    {
        accessorKey: "createdAt",
        header: "Date Added",
        cell: ({row}) => (
            <div className="text-muted-foreground">
                {format(new Date(row.original.createdAt), "MMM d, yyyy")}
            </div>
        ),
    },
    createActionsColumn<SystemAdmin>([
        {
            icon: IconKey,
            label: "Reset Password",
            onClick: (row, table) => table.options.meta?.openResetPasswordDialog?.(row.original),
        },
        {
            icon: IconTrash,
            label: "Delete Admin",
            isDestructive: true,
            onClick: (row, table) => table.options.meta?.openDeleteDialog?.(row.original),
        }
    ]),
];
