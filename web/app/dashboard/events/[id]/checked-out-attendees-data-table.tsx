"use client";

import * as React from "react";
import {ColumnDef} from "@tanstack/react-table";
import {CheckedInAttendeeResponse} from "@/types";
import {SimpleFilteredDataTable} from "@/components/shared/simple-filtered-data-table";

interface CheckedOutAttendeesDataTableProps {
    columns: ColumnDef<CheckedInAttendeeResponse>[];
    data: CheckedInAttendeeResponse[];
    isLoading: boolean;
    pageCount: number;
    pagination: { pageIndex: number; pageSize: number; };
    setPagination: (pagination: { pageIndex: number; pageSize: number; }) => void;
}

export function CheckedOutAttendeesDataTable(props: CheckedOutAttendeesDataTableProps) {
    return (
        <SimpleFilteredDataTable
            {...props}
            filterPlaceholder="Filter attendees by name..."
        />
    );
}
