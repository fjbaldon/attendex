"use client";

import {ColumnDef} from "@tanstack/react-table";
import {ScannerResponse} from "@/types";
import {IconKey, IconTrash} from "@tabler/icons-react";
import {selectColumn} from "@/components/shared/data-table-columns";
import {createActionsColumn} from "@/components/shared/data-table-action-column";

export const getColumns = (): ColumnDef<ScannerResponse>[] => [
    selectColumn<ScannerResponse>(),
    {
        accessorKey: "email",
        header: "Email Address",
        cell: ({row}) => {
            return <div className="font-medium">{row.original.email}</div>;
        }
    },
    createActionsColumn<ScannerResponse>([
        {
            icon: IconKey,
            label: "Reset Password",
            onClick: (row, table) => table.options.meta?.openResetPasswordDialog?.(row.original),
        },
        {
            icon: IconTrash,
            label: "Remove Scanner",
            isDestructive: true,
            onClick: (row, table) => table.options.meta?.openDeleteDialog?.(row.original),
        }
    ]),
];
