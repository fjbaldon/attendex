"use client";

import {ColumnDef} from "@tanstack/react-table";
import {AttendeeResponse, Attribute} from "@/types";
import {createAttendeeBaseColumns} from "../../attendees/attendee-column-helper";
import {createActionsColumn} from "@/components/shared/data-table-action-column";
import {selectColumn} from "@/components/shared/data-table-columns";
import {IconTrash} from "@tabler/icons-react";

export const getColumns = (attributes: Attribute[]): ColumnDef<AttendeeResponse>[] => {
    const baseColumns = createAttendeeBaseColumns<AttendeeResponse>(attributes);

    const actionColumn = createActionsColumn<AttendeeResponse>([
        {
            icon: IconTrash,
            label: "Remove from Event",
            isDestructive: true,
            onClick: (row, table) => table.options.meta?.openDeleteDialog?.(row.original),
        },
    ]);

    return [selectColumn<AttendeeResponse>(), ...baseColumns, actionColumn];
};
