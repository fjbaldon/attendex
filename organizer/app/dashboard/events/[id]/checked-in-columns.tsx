"use client";

import {ColumnDef} from "@tanstack/react-table";
import {AttendeeResponse, CustomFieldDefinition} from "@/types";
import {Checkbox} from "@/components/ui/checkbox";

export const getCheckedInColumns = (customFieldDefs: CustomFieldDefinition[]): ColumnDef<AttendeeResponse>[] => {
    const standardColumns: ColumnDef<AttendeeResponse>[] = [
        {
            id: "select",
            header: ({table}) => (
                <Checkbox
                    checked={table.getIsAllPageRowsSelected() || (table.getIsSomePageRowsSelected() && "indeterminate")}
                    onCheckedChange={(value) => table.toggleAllPageRowsSelected(!!value)}
                    aria-label="Select all"
                />
            ),
            cell: ({row}) => (
                <Checkbox
                    checked={row.getIsSelected()}
                    onCheckedChange={(value) => row.toggleSelected(!!value)}
                    aria-label="Select row"
                />
            ),
            enableSorting: false,
            enableHiding: false,
        },
        {
            accessorKey: "uniqueIdentifier",
            header: "Identifier",
        },
        {
            accessorKey: "lastName",
            header: "Last Name",
            cell: ({row}) => <div className="font-medium">{row.original.lastName}</div>,
        },
        {
            accessorKey: "firstName",
            header: "First Name",
            cell: ({row}) => <div className="font-medium">{row.original.firstName}</div>,
        },
    ];

    const customFieldColumns: ColumnDef<AttendeeResponse>[] = customFieldDefs.map(def => ({
        accessorKey: `customFields.${def.fieldName}`,
        header: def.fieldName,
        cell: ({getValue}) => {
            const value = getValue<unknown>();
            if (value === null || value === undefined || value === '') {
                return <span className="text-muted-foreground text-xs italic">N/A</span>;
            }
            return String(value);
        }
    }));

    const actionColumnPlaceholder: ColumnDef<AttendeeResponse> = {
        id: "actions",
        cell: () => {
            return <div className="flex justify-end h-8 w-8 p-0"/>;
        },
    };

    return [...standardColumns, ...customFieldColumns, actionColumnPlaceholder];
};
