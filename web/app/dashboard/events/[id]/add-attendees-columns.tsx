"use client";

import {ColumnDef} from "@tanstack/react-table";
import {AttendeeResponse, Attribute} from "@/types";
import {Checkbox} from "@/components/ui/checkbox";

export const getColumns = (attributes: Attribute[]): ColumnDef<AttendeeResponse>[] => {
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
            accessorKey: "identity",
            header: "Identity",
        },
        {
            accessorKey: "lastName",
            header: "Last Name",
        },
        {
            accessorKey: "firstName",
            header: "First Name",
        },
    ];

    const attributeColumns: ColumnDef<AttendeeResponse>[] = attributes.map(def => ({
        accessorKey: `attributes.${def.name}`,
        header: def.name,
        cell: ({getValue}) => {
            const value = getValue<unknown>();
            if (value === null || value === undefined || value === '') {
                return <span className="text-muted-foreground text-xs italic">N/A</span>;
            }
            return String(value);
        }
    }));

    return [...standardColumns, ...attributeColumns];
};
