"use client";

import {ColumnDef} from "@tanstack/react-table";
import {Attribute} from "@/types";
import {Button} from "@/components/ui/button";

export const createAttendeeBaseColumns = <TData, >(
    attributes: Attribute[],
    baseAccessor?: keyof TData | ''
): ColumnDef<TData>[] => {

    const accessorPrefix = baseAccessor ? `${String(baseAccessor)}.` : '';

    const standardColumns: ColumnDef<TData>[] = [
        {
            accessorKey: `${accessorPrefix}identity`,
            header: "Identifier",
            cell: ({row, getValue, table}) => {
                const value = String(getValue());
                // Check if the View Dialog action is available
                if (table.options.meta?.openViewDialog) {
                    return (
                        <Button
                            variant="link"
                            className="p-0 h-auto font-normal text-foreground hover:underline"
                            onClick={() => table.options.meta?.openViewDialog?.(row.original)}
                        >
                            {value}
                        </Button>
                    );
                }
                return value;
            }
        },
        {
            accessorKey: `${accessorPrefix}lastName`,
            header: "Last Name",
            cell: ({row, getValue, table}) => {
                const value = String(getValue());
                if (table.options.meta?.openViewDialog) {
                    return (
                        <Button
                            variant="link"
                            className="p-0 h-auto font-medium text-foreground hover:underline"
                            onClick={() => table.options.meta?.openViewDialog?.(row.original)}
                        >
                            {value}
                        </Button>
                    );
                }
                return <div className="font-medium">{value}</div>;
            },
        },
        {
            accessorKey: `${accessorPrefix}firstName`,
            header: "First Name",
            cell: ({row, getValue, table}) => {
                const value = String(getValue());
                if (table.options.meta?.openViewDialog) {
                    return (
                        <Button
                            variant="link"
                            className="p-0 h-auto font-medium text-foreground hover:underline"
                            onClick={() => table.options.meta?.openViewDialog?.(row.original)}
                        >
                            {value}
                        </Button>
                    );
                }
                return <div className="font-medium">{value}</div>;
            },
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
