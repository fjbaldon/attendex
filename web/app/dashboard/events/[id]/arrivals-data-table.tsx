"use client";

import * as React from "react";
import {ColumnDef} from "@tanstack/react-table";
import {EntryDetailsDto} from "@/types";
import {SimpleFilteredDataTable} from "@/components/shared/simple-filtered-data-table";

interface ArrivalsDataTableProps {
    columns: ColumnDef<EntryDetailsDto>[];
    data: EntryDetailsDto[];
    isLoading: boolean;
    pageCount: number;
    pagination: { pageIndex: number; pageSize: number; };
    setPagination: (pagination: { pageIndex: number; pageSize: number; }) => void;
}

export function ArrivalsDataTable(props: ArrivalsDataTableProps) {
    return (
        <SimpleFilteredDataTable
            {...props}
            filterPlaceholder="Filter by name..."
            filterColumnFn={(item) => `${item.attendee.firstName} ${item.attendee.lastName}`}
        />
    );
}
