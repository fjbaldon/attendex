"use client";

import {ColumnDef} from "@tanstack/react-table";
import {AttendeeResponse, Attribute} from "@/types";
import {IconPencil, IconTrash} from "@tabler/icons-react";
import {createAttendeeBaseColumns} from "./attendee-column-helper";
import {createActionsColumn} from "@/components/shared/data-table-action-column";
import {selectColumn} from "@/components/shared/data-table-columns";

export const getColumns = (attributes: Attribute[]): ColumnDef<AttendeeResponse>[] => {
    const baseColumns = createAttendeeBaseColumns<AttendeeResponse>(attributes);

    const actionsColumn = createActionsColumn<AttendeeResponse>([
        {
            icon: IconPencil,
            label: "Edit Attendee",
            onClick: (row, table) => table.options.meta?.openEditDialog?.(row.original),
        },
        {
            icon: IconTrash,
            label: "Remove Attendee",
            isDestructive: true,
            onClick: (row, table) => table.options.meta?.openDeleteDialog?.(row.original),
        },
    ]);

    return [selectColumn<AttendeeResponse>(), ...baseColumns, actionsColumn];
};
