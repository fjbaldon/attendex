"use client";

import {ColumnDef} from "@tanstack/react-table";
import {CheckedInAttendeeResponse, CustomFieldDefinition} from "@/types";
import {Checkbox} from "@/components/ui/checkbox";
import {format} from 'date-fns';

export const getCheckedOutColumns = (customFieldDefs: CustomFieldDefinition[]): ColumnDef<CheckedInAttendeeResponse>[] => {
    const standardColumns: ColumnDef<CheckedInAttendeeResponse>[] = [
        {
            id: "select",
            header: ({table}) => (<Checkbox checked={table.getIsAllPageRowsSelected()}
                                            onCheckedChange={(value) => table.toggleAllPageRowsSelected(!!value)}
                                            aria-label="Select all"/>),
            cell: ({row}) => (
                <Checkbox checked={row.getIsSelected()} onCheckedChange={(value) => row.toggleSelected(!!value)}
                          aria-label="Select row"/>),
            enableSorting: false,
            enableHiding: false,
        },
        {accessorKey: "uniqueIdentifier", header: "Identifier"},
        {
            accessorKey: "lastName",
            header: "Last Name",
            cell: ({row}) => <div className="font-medium">{row.original.lastName}</div>
        },
        {
            accessorKey: "firstName",
            header: "First Name",
            cell: ({row}) => <div className="font-medium">{row.original.firstName}</div>
        },
        {
            accessorKey: "checkInTimestamp",
            header: "Checked-out Time", // <-- Changed header
            cell: ({row}) => {
                const timestamp = row.original.checkInTimestamp;
                if (!timestamp) return <div className="text-sm text-muted-foreground italic">N/A</div>;
                try {
                    const formattedTime = format(new Date(timestamp), "h:mm:ss a");
                    return <div className="text-sm text-muted-foreground">{formattedTime}</div>;
                } catch {
                    return <div className="text-sm text-destructive">Invalid Date</div>;
                }
            },
        },
    ];

    const customFieldColumns: ColumnDef<CheckedInAttendeeResponse>[] = customFieldDefs.map(def => ({
        accessorKey: `customFields.${def.fieldName}`,
        header: def.fieldName,
        cell: ({getValue}) => {
            const value = getValue<unknown>();
            return (value === null || value === undefined || value === '') ?
                <span className="text-muted-foreground text-xs italic">N/A</span> : String(value);
        }
    }));

    const actionColumnPlaceholder: ColumnDef<CheckedInAttendeeResponse> = {
        id: "actions",
        cell: () => <div className="flex justify-end h-8 w-8 p-0"/>
    };

    return [...standardColumns, ...customFieldColumns, actionColumnPlaceholder];
};
