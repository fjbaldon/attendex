"use client";

import * as React from "react";
import {ColumnDef} from "@tanstack/react-table";
import {EntryDetailsDto} from "@/types";
import {Input} from "@/components/ui/input";
import {DataTable} from "@/components/shared/data-table";

interface DeparturesDataTableProps {
    columns: ColumnDef<EntryDetailsDto>[];
    data: EntryDetailsDto[];
    isLoading: boolean;
    pageCount: number;
    pagination: { pageIndex: number; pageSize: number; };
    setPagination: (pagination: { pageIndex: number; pageSize: number; }) => void;
    // FIX: Search props
    searchQuery: string;
    onSearchChange: (val: string) => void;
}

export function DeparturesDataTable({
                                        columns,
                                        data,
                                        isLoading,
                                        pageCount,
                                        pagination,
                                        setPagination,
                                        searchQuery,
                                        onSearchChange
                                    }: DeparturesDataTableProps) {
    const toolbar = (
        <div className="flex items-center justify-between">
            <Input
                placeholder="Filter by name..."
                value={searchQuery}
                onChange={(event) => onSearchChange(event.target.value)}
                className="h-9 max-w-sm"
            />
        </div>
    );

    return (
        <DataTable
            columns={columns}
            data={data}
            isLoading={isLoading}
            pageCount={pageCount}
            pagination={pagination}
            setPagination={setPagination}
            toolbar={toolbar}
        />
    );
}
