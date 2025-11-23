"use client";

import {ColumnDef} from "@tanstack/react-table";
import {OrganizerResponse} from "@/types";
import {IconKey, IconTrash} from "@tabler/icons-react";
import {createActionsColumn} from "@/components/shared/data-table-action-column";

export const columns: ColumnDef<OrganizerResponse>[] = [
    {
        accessorKey: "email",
        header: () => <div className="pl-4">Email Address</div>,
        cell: ({row}) => {
            return <div className="pl-4 font-medium">{row.original.email}</div>;
        }
    },
    createActionsColumn<OrganizerResponse>([
        {
            icon: IconKey,
            label: "Reset Password",
            onClick: (row, table) => table.options.meta?.openResetPasswordDialog?.(row.original),
        },
        {
            icon: IconTrash,
            label: "Remove Organizer",
            isDestructive: true,
            onClick: (row, table) => table.options.meta?.openDeleteDialog?.(row.original),
        }
    ]),
];
