"use client";

import {ColumnDef} from "@tanstack/react-table";
import {CheckedInAttendeeResponse, CustomFieldDefinition} from "@/types";
import {format} from 'date-fns';
import {createAttendeeBaseColumns} from "../../attendees/attendee-column-helper";

export const getCheckedInColumns = (customFieldDefs: CustomFieldDefinition[]): ColumnDef<CheckedInAttendeeResponse>[] => {
    const baseColumns = createAttendeeBaseColumns<CheckedInAttendeeResponse>(customFieldDefs);

    const timestampColumn: ColumnDef<CheckedInAttendeeResponse> = {
        accessorKey: "checkInTimestamp",
        header: "Checked-in Time",
        cell: ({row}) => {
            const timestamp = row.original.checkInTimestamp;
            if (!timestamp) return <div className="text-sm text-muted-foreground italic">N/A</div>;
            try {
                return <div className="text-sm text-muted-foreground">{format(new Date(timestamp), "h:mm:ss a")}</div>;
            } catch {
                return <div className="text-sm text-destructive">Invalid Date</div>;
            }
        },
    };

    return [...baseColumns, timestampColumn];
};
