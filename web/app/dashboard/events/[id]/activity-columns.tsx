"use client";

import {ColumnDef} from "@tanstack/react-table";
import {Attribute, EntryDetailsDto, Session} from "@/types";
import {format} from 'date-fns';
import {createAttendeeBaseColumns} from "../../attendees/attendee-column-helper";
import {selectColumn} from "@/components/shared/data-table-columns";
import {Badge} from "@/components/ui/badge";

export const getActivityColumns = (attributes: Attribute[], sessions: Session[] = []): ColumnDef<EntryDetailsDto>[] => {
    const baseColumns = createAttendeeBaseColumns<EntryDetailsDto>(attributes, 'attendee');

    const sessionColumn: ColumnDef<EntryDetailsDto> = {
        accessorKey: "sessionId",
        header: "Session",
        cell: ({row}) => {
            const sid = row.original.sessionId;
            if (!sid) return <span className="text-muted-foreground italic text-xs">Unscheduled</span>;

            const session = sessions.find(s => s.id === sid);
            return (
                <div className="flex flex-col">
                    <span className="font-medium text-sm">{session?.activityName || "Unknown Session"}</span>
                    <span className="text-[10px] text-muted-foreground">{session?.intent}</span>
                </div>
            );
        }
    };

    const timestampColumn: ColumnDef<EntryDetailsDto> = {
        accessorKey: "scanTimestamp",
        header: "Time",
        cell: ({row}) => {
            const timestamp = row.original.scanTimestamp;
            if (!timestamp) return <div className="text-sm text-muted-foreground italic">N/A</div>;
            try {
                return <div className="text-sm font-mono">{format(new Date(timestamp), "h:mm:ss a")}</div>;
            } catch {
                return <div className="text-sm text-destructive">Invalid</div>;
            }
        },
    };

    const punctualityColumn: ColumnDef<EntryDetailsDto> = {
        accessorKey: "punctuality",
        header: "Status",
        cell: ({row}) => {
            const p = row.original.punctuality.toLowerCase();
            let color = "bg-gray-100 text-gray-800";
            if(p === 'punctual') color = "bg-green-100 text-green-800 border-green-200";
            if(p === 'late') color = "bg-amber-100 text-amber-800 border-amber-200";
            if(p === 'early') color = "bg-blue-100 text-blue-800 border-blue-200";

            return <Badge variant="outline" className={`capitalize ${color}`}>{p}</Badge>;
        },
    };

    return [
        selectColumn<EntryDetailsDto>(),
        ...baseColumns,
        sessionColumn,
        timestampColumn,
        punctualityColumn
    ];
};
