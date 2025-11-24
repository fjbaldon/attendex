"use client";

import {ColumnDef} from "@tanstack/react-table";
import {Attribute, EntryDetailsDto} from "@/types";
import {format} from 'date-fns';
import {createAttendeeBaseColumns} from "../../attendees/attendee-column-helper";
import {selectColumn} from "@/components/shared/data-table-columns";

export const getDeparturesColumns = (attributes: Attribute[]): ColumnDef<EntryDetailsDto>[] => {
    const baseColumns = createAttendeeBaseColumns<EntryDetailsDto>(attributes, 'attendee');

    const timestampColumn: ColumnDef<EntryDetailsDto> = {
        accessorKey: "scanTimestamp",
        header: "Departure Time",
        cell: ({row}) => {
            const timestamp = row.original.scanTimestamp;
            if (!timestamp) return <div className="text-sm text-muted-foreground italic">N/A</div>;
            try {
                return <div className="text-sm">{format(new Date(timestamp), "h:mm:ss a")}</div>;
            } catch {
                return <div className="text-sm text-destructive">Invalid Date</div>;
            }
        },
    };

    // ADDED: Status Column
    const punctualityColumn: ColumnDef<EntryDetailsDto> = {
        accessorKey: "punctuality",
        header: "Status",
        cell: ({row}) => (
            <div className="text-sm capitalize text-muted-foreground">
                {row.original.punctuality.toLowerCase()}
            </div>
        ),
    };

    return [
        selectColumn<EntryDetailsDto>(),
        ...baseColumns,
        timestampColumn,
        punctualityColumn // Added here
    ];
};
