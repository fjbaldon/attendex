"use client";

import {ColumnDef} from "@tanstack/react-table";
import {CustomFieldDefinition} from "@/types";
import {selectColumn} from "@/components/shared/data-table-columns";

type AttendeeLike = {
    id: number;
    uniqueIdentifier: string;
    firstName: string;
    lastName: string;
    customFields?: Record<string, unknown>;
};

export const createAttendeeBaseColumns = <T extends AttendeeLike>(
    customFieldDefs: CustomFieldDefinition[]
): ColumnDef<T>[] => {
    const standardColumns: ColumnDef<T>[] = [
        selectColumn<T>(),
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

    const customFieldColumns: ColumnDef<T>[] = customFieldDefs.map(def => ({
        accessorKey: `customFields.${def.fieldName}`,
        header: def.fieldName,
        cell: ({getValue}) => {
            const value = getValue<unknown>();
            if (value === null || value === undefined || value === '') {
                return <span className="text-muted-foreground text-sm italic">N/A</span>;
            }
            return String(value);
        }
    }));

    return [...standardColumns, ...customFieldColumns];
};
