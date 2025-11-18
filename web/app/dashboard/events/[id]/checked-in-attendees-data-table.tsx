"use client";

import * as React from "react";
import {ColumnDef} from "@tanstack/react-table";
import {CheckedInAttendeeResponse} from "@/types";
import {SimpleFilteredDataTable} from "@/components/shared/simple-filtered-data-table";

interface CheckedInAttendeesDataTableProps {
    columns: ColumnDef<CheckedInAttendeeResponse>[];
    data: CheckedInAttendeeResponse[];
    isLoading: boolean;
    pageCount: number;
    pagination: { pageIndex: number; pageSize: number; };
    setPagination: (pagination: { pageIndex: number; pageSize: number; }) => void;
}

export function CheckedInAttendeesDataTable(props: CheckedInAttendeesDataTableProps) {
    return (
        <SimpleFilteredDataTable
            {...props}
            filterPlaceholder="Filter attendees by name..."
        />
    );
}
