"use client";

import * as React from "react";
import {ColumnDef} from "@tanstack/react-table";
import {Attribute, EntryDetailsDto} from "@/types";
import {DataTable} from "@/components/shared/data-table";
import {FilterToolbar} from "@/components/shared/filter-toolbar";

interface ActivityDataTableProps {
    columns: ColumnDef<EntryDetailsDto>[];
    data: EntryDetailsDto[];
    isLoading: boolean;
    pageCount: number;
    pagination: { pageIndex: number; pageSize: number; };
    // FIX: Typed as React.Dispatch to allow functional updates (prev => ...)
    setPagination: React.Dispatch<React.SetStateAction<{ pageIndex: number; pageSize: number; }>>;
    searchQuery: string;
    onSearchChange: (val: string) => void;
    activeFilters: Record<string, string>;
    onFiltersChange: (filters: Record<string, string>) => void;
    attributes: Attribute[];
    toolbarChildren?: React.ReactNode;
}

export function ActivityDataTable({
                                      columns,
                                      data,
                                      isLoading,
                                      pageCount,
                                      pagination,
                                      setPagination,
                                      searchQuery,
                                      onSearchChange,
                                      activeFilters,
                                      onFiltersChange,
                                      attributes,
                                      toolbarChildren
                                  }: ActivityDataTableProps) {

    const toolbar = (
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between w-full">
            <FilterToolbar
                searchQuery={searchQuery}
                onSearchChange={(val) => {
                    onSearchChange(val);
                    setPagination((prev) => ({...prev, pageIndex: 0}));
                }}
                searchPlaceholder="Search attendee..."
                activeFilters={activeFilters}
                onFiltersChange={(filters) => {
                    onFiltersChange(filters);
                    setPagination((prev) => ({...prev, pageIndex: 0}));
                }}
                attributes={attributes}
            >
                {toolbarChildren}
            </FilterToolbar>
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
