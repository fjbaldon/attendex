"use client";

import * as React from "react";
import {
    ColumnDef,
    ColumnFiltersState,
    flexRender,
    getCoreRowModel,
    getSortedRowModel,
    SortingState,
    useReactTable,
} from "@tanstack/react-table";
import {
    IconPlus,
} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import {AttendeeResponse} from "@/types";
import {DataTablePagination} from "@/components/shared/data-table-pagination";

interface AttendeesDataTableProps {
    columns: ColumnDef<AttendeeResponse>[];
    data: AttendeeResponse[];
    isLoading: boolean;
    pageCount: number;
    pagination: { pageIndex: number; pageSize: number; };
    setPagination: (pagination: { pageIndex: number; pageSize: number; }) => void;
}

export function AttendeesDataTable({
                                       columns,
                                       data,
                                       isLoading,
                                       pageCount,
                                       pagination,
                                       setPagination
                                   }: AttendeesDataTableProps) {
    const [sorting, setSorting] = React.useState<SortingState>([]);
    const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([]);
    const [rowSelection, setRowSelection] = React.useState({});

    const table = useReactTable({
        data,
        columns,
        pageCount,
        state: {
            sorting,
            columnFilters,
            rowSelection,
            pagination,
        },
        getRowId: (row) => String(row.id),
        enableRowSelection: true,
        onRowSelectionChange: setRowSelection,
        onSortingChange: setSorting,
        onColumnFiltersChange: setColumnFilters,
        onPaginationChange: (updater) => {
            if (typeof updater === 'function') {
                const newPagination = updater(pagination);
                setPagination(newPagination);
            } else {
                setPagination(updater);
            }
        },
        getCoreRowModel: getCoreRowModel(),
        getSortedRowModel: getSortedRowModel(),
        manualPagination: true,
    });

    return (
        <div className="flex w-full flex-col justify-start gap-4">
            <div className="flex items-center justify-between">
                <Input
                    placeholder="Filter attendees..."
                    className="h-9 max-w-sm"
                />
                <div className="flex items-center gap-2">
                    <Button size="sm" variant="outline" className="h-9">
                        Import
                    </Button>
                    <Button size="sm" className="h-9">
                        <IconPlus className="mr-2 h-4 w-4"/>
                        <span>Add Attendee</span>
                    </Button>
                </div>
            </div>
            <div className="overflow-hidden rounded-lg border">
                <Table>
                    <TableHeader className="bg-muted sticky top-0 z-10">
                        {table.getHeaderGroups().map((headerGroup) => (
                            <TableRow key={headerGroup.id}>
                                {headerGroup.headers.map((header) => (
                                    <TableHead key={header.id} colSpan={header.colSpan}>
                                        {header.isPlaceholder ? null : flexRender(header.column.columnDef.header, header.getContext())}
                                    </TableHead>
                                ))}
                            </TableRow>
                        ))}
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={columns.length} className="h-24 text-center">
                                    Loading attendees...
                                </TableCell>
                            </TableRow>
                        ) : table.getRowModel().rows?.length ? (
                            table.getRowModel().rows.map((row) => (
                                <TableRow key={row.id} data-state={row.getIsSelected() && "selected"}>
                                    {row.getVisibleCells().map((cell) => (
                                        <TableCell key={cell.id}>
                                            {flexRender(cell.column.columnDef.cell, cell.getContext())}
                                        </TableCell>
                                    ))}
                                </TableRow>
                            ))
                        ) : (
                            <TableRow>
                                <TableCell colSpan={columns.length} className="h-24 text-center">
                                    No attendees found.
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </div>
            <div className="pt-2">
                <DataTablePagination table={table}/>
            </div>
        </div>
    );
}
