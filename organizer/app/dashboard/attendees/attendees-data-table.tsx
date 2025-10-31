"use client";

import * as React from "react";
import {
    ColumnDef,
    ColumnFiltersState,
    flexRender,
    getCoreRowModel,
    getFilteredRowModel,
    getPaginationRowModel,
    getSortedRowModel,
    SortingState,
    useReactTable,
} from "@tanstack/react-table";
import {IconPlus, IconUpload} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {AttendeeResponse} from "@/types";
import {useAttendees} from "@/hooks/use-attendees";
import {ConfirmDialog} from "@/components/shared/confirm-dialog";
import {DataTablePagination} from "@/components/shared/data-table-pagination";
import {Input} from "@/components/ui/input";
import {AttendeeDialog} from "./attendee-dialog";
import {AttendeeImportDialog} from "@/components/attendees/attendee-import-dialog";

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
    const {deleteAttendee, isDeletingAttendee} = useAttendees();

    const [sorting, setSorting] = React.useState<SortingState>([]);
    const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([]);
    const [rowSelection, setRowSelection] = React.useState({});
    const [isFormDialogOpen, setIsFormDialogOpen] = React.useState(false);
    const [isConfirmDialogOpen, setIsConfirmDialogOpen] = React.useState(false);
    const [isImportDialogOpen, setIsImportDialogOpen] = React.useState(false);
    const [selectedAttendee, setSelectedAttendee] = React.useState<AttendeeResponse | null>(null);

    const table = useReactTable({
        data,
        columns,
        pageCount,
        state: {sorting, columnFilters, rowSelection, pagination},
        onSortingChange: setSorting,
        onColumnFiltersChange: setColumnFilters,
        onRowSelectionChange: setRowSelection,
        getCoreRowModel: getCoreRowModel(),
        getSortedRowModel: getSortedRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        onPaginationChange: (updater) => {
            if (typeof updater === 'function') {
                setPagination(updater(pagination));
            } else {
                setPagination(updater);
            }
        },
        manualPagination: true,
        meta: {
            openEditDialog: (attendee: AttendeeResponse) => {
                setSelectedAttendee(attendee);
                setIsFormDialogOpen(true);
            },
            openDeleteDialog: (attendee: AttendeeResponse) => {
                setSelectedAttendee(attendee);
                setIsConfirmDialogOpen(true);
            },
        },
    });

    const handleOpenCreateDialog = () => {
        setSelectedAttendee(null);
        setIsFormDialogOpen(true);
    };

    const handleDeleteConfirm = () => {
        if (selectedAttendee) {
            deleteAttendee(selectedAttendee.id, {
                onSuccess: () => setIsConfirmDialogOpen(false),
            });
        }
    };

    return (
        <>
            <AttendeeDialog
                open={isFormDialogOpen}
                onOpenChange={setIsFormDialogOpen}
                attendee={selectedAttendee}
            />
            <AttendeeImportDialog
                open={isImportDialogOpen}
                onOpenChange={setIsImportDialogOpen}
            />
            <ConfirmDialog
                open={isConfirmDialogOpen}
                onOpenChange={setIsConfirmDialogOpen}
                onConfirm={handleDeleteConfirm}
                title="Are you sure?"
                description={`This will permanently delete ${selectedAttendee?.firstName} ${selectedAttendee?.lastName}. This action cannot be undone.`}
                isLoading={isDeletingAttendee}
            />
            <div className="flex w-full flex-col justify-start gap-4">
                <div className="flex items-center justify-between">
                    <Input
                        placeholder="Filter attendees by name..."
                        value={(table.getColumn("lastName")?.getFilterValue() as string) ?? ""}
                        onChange={(event) =>
                            table.getColumn("lastName")?.setFilterValue(event.target.value)
                        }
                        className="h-9 max-w-sm"
                    />
                    <div className="flex items-center gap-2">
                        <Button size="sm" variant="outline" className="h-9" onClick={() => setIsImportDialogOpen(true)}>
                            <IconUpload className="mr-2 h-4 w-4"/>
                            Import CSV
                        </Button>
                        <Button size="sm" className="h-9" onClick={handleOpenCreateDialog}>
                            <IconPlus className="mr-2 h-4 w-4"/>
                            <span>Add Attendee</span>
                        </Button>
                    </div>
                </div>
                <div className="rounded-md border">
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
        </>
    );
}
