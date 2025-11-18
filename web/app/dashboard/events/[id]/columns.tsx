"use client";

import {ColumnDef} from "@tanstack/react-table";
import {AttendeeResponse, CustomFieldDefinition} from "@/types";
import {IconTrash} from "@tabler/icons-react";
import {createAttendeeBaseColumns} from "../../attendees/attendee-column-helper";
import {createActionsColumn} from "@/components/shared/data-table-action-column";

export const getColumns = (customFieldDefs: CustomFieldDefinition[]): ColumnDef<AttendeeResponse>[] => {
    const baseColumns = createAttendeeBaseColumns<AttendeeResponse>(customFieldDefs);

    const actionColumn = createActionsColumn<AttendeeResponse>([
        {
            icon: IconTrash,
            label: "Remove from Event",
            isDestructive: true,
            onClick: (row, table) => table.options.meta?.openDeleteDialog?.(row.original),
        },
    ]);

    return [...baseColumns, actionColumn];
};
