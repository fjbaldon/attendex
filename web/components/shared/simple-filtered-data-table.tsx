"use client";

import * as React from "react";
import {ColumnDef} from "@tanstack/react-table";
import {Input} from "@/components/ui/input";
import {DataTable} from "@/components/shared/data-table";

interface SimpleFilteredDataTableProps<TData extends { firstName: string; lastName: string }> {
    columns: ColumnDef<TData>[];
    data: TData[];
    isLoading: boolean;
    pageCount: number;
    pagination: { pageIndex: number; pageSize: number };
    setPagination: (pagination: { pageIndex: number; pageSize: number }) => void;
    filterPlaceholder: string;
}

export function SimpleFilteredDataTable<TData extends { firstName: string; lastName: string }>({
                                                                                                   data,
                                                                                                   filterPlaceholder,
                                                                                                   ...props
                                                                                               }: SimpleFilteredDataTableProps<TData>) {
    const [filter, setFilter] = React.useState("");

    const toolbar = (
        <div className="flex items-center justify-between">
            <Input
                placeholder={filterPlaceholder}
                value={filter}
                onChange={(event) => setFilter(event.target.value)}
                className="h-9 max-w-sm"
            />
        </div>
    );

    const filteredData = React.useMemo(() =>
        data.filter(item =>
            `${item.firstName} ${item.lastName}`.toLowerCase().includes(filter.toLowerCase())
        ), [data, filter]);

    return (
        <DataTable
            {...props}
            data={filteredData}
            toolbar={toolbar}
        />
    );
}
