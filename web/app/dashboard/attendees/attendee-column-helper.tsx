"use client";

import {ColumnDef} from "@tanstack/react-table";
import {Attribute} from "@/types";

export const createAttendeeBaseColumns = <TData, >(
    attributes: Attribute[],
    baseAccessor?: keyof TData | ''
): ColumnDef<TData>[] => {

    const accessorPrefix = baseAccessor ? `${String(baseAccessor)}.` : '';

    const standardColumns: ColumnDef<TData>[] = [
        {
            accessorKey: `${accessorPrefix}identity`,
            header: "Identifier",
        },
        {
            accessorKey: `${accessorPrefix}lastName`,
            header: "Last Name",
            cell: ({getValue}) => <div className="font-medium">{String(getValue())}</div>,
        },
        {
            accessorKey: `${accessorPrefix}firstName`,
            header: "First Name",
            cell: ({getValue}) => <div className="font-medium">{String(getValue())}</div>,
        },
    ];

    const attributeColumns: ColumnDef<TData>[] = attributes.map(def => ({
        accessorKey: `${accessorPrefix}attributes.${def.name}`,
        header: def.name,
        cell: ({getValue}) => {
            const value = getValue<unknown>();
            if (value === null || value === undefined || value === '') {
                return <span className="text-muted-foreground text-sm italic">N/A</span>;
            }
            return String(value);
        }
    }));

    return [...standardColumns, ...attributeColumns];
};
