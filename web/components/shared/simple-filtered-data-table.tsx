"use client";

import * as React from "react";
import {ColumnDef} from "@tanstack/react-table";
import {Input} from "@/components/ui/input";
import {DataTable} from "@/components/shared/data-table";

interface SimpleFilteredDataTableProps<TData> {
    columns: ColumnDef<TData>[];
    data: TData[];
    isLoading: boolean;
    pageCount: number;
    pagination: { pageIndex: number; pageSize: number };
    setPagination: (pagination: { pageIndex: number; pageSize: number }) => void;
    filterPlaceholder: string;
    filterColumnFn: (item: TData) => string;
}

export function SimpleFilteredDataTable<TData>({
                                                   data,
                                                   filterPlaceholder,
                                                   filterColumnFn,
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
            filterColumnFn(item).toLowerCase().includes(filter.toLowerCase())
        ), [data, filter, filterColumnFn]);

    return (
        <DataTable
            {...props}
            data={filteredData}
            toolbar={toolbar}
        />
    );
}
