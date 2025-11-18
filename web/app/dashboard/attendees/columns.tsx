"use client";

import {ColumnDef} from "@tanstack/react-table";
import {AttendeeResponse, CustomFieldDefinition} from "@/types";
import {IconPencil, IconTrash} from "@tabler/icons-react";
import {createAttendeeBaseColumns} from "./attendee-column-helper";
import {createActionsColumn} from "@/components/shared/data-table-action-column";

export const getColumns = (customFieldDefs: CustomFieldDefinition[]): ColumnDef<AttendeeResponse>[] => {
    const baseColumns = createAttendeeBaseColumns<AttendeeResponse>(customFieldDefs);

    const actionsColumn = createActionsColumn<AttendeeResponse>([
        {
            icon: IconPencil,
            label: "Edit Attendee",
            onClick: (row, table) => table.options.meta?.openEditDialog?.(row.original),
        },
        {
            icon: IconTrash,
            label: "Delete Attendee",
            isDestructive: true,
            onClick: (row, table) => table.options.meta?.openDeleteDialog?.(row.original),
        },
    ]);

    return [...baseColumns, actionsColumn];
};
