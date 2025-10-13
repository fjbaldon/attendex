"use client";

import * as React from "react";
import {
    ColumnDef,
    flexRender,
    getCoreRowModel,
    getFilteredRowModel,
    getPaginationRowModel,
    getSortedRowModel,
    useReactTable,
    SortingState,
    ColumnFiltersState,
} from "@tanstack/react-table";
import {IconPlus} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {AttendeeResponse} from "@/types";
import {ConfirmDialog} from "@/components/shared/confirm-dialog";
import {DataTablePagination} from "@/components/shared/data-table-pagination";
import {Input} from "@/components/ui/input";
import {useEventDetails} from "@/hooks/use-event-details";
import {AddAttendeeDialog} from "./add-attendee-dialog";

interface EventAttendeesDataTableProps {
    columns: ColumnDef<AttendeeResponse>[];
    data: AttendeeResponse[];
    isLoading: boolean;
    eventId: number;
}

export function EventAttendeesDataTable({columns, data, isLoading, eventId}: EventAttendeesDataTableProps) {
    const {removeAttendee, isRemovingAttendee} = useEventDetails(eventId);

    const [sorting, setSorting] = React.useState<SortingState>([]);
    const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([]);
    const [rowSelection, setRowSelection] = React.useState({});
    const [isAddDialogOpen, setIsAddDialogOpen] = React.useState(false);
    const [isConfirmDialogOpen, setIsConfirmDialogOpen] = React.useState(false);
    const [selectedAttendee, setSelectedAttendee] = React.useState<AttendeeResponse | null>(null);

    const table = useReactTable({
        data,
        columns,
        state: {sorting, columnFilters, rowSelection},
        onSortingChange: setSorting,
        onColumnFiltersChange: setColumnFilters,
        onRowSelectionChange: setRowSelection,
        getCoreRowModel: getCoreRowModel(),
        getSortedRowModel: getSortedRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        meta: {
            openDeleteDialog: (attendee: AttendeeResponse) => {
                setSelectedAttendee(attendee);
                setIsConfirmDialogOpen(true);
            },
        },
    });

    const handleDeleteConfirm = () => {
        if (selectedAttendee) {
            removeAttendee({eventId, attendeeId: selectedAttendee.id}, {
                onSuccess: () => setIsConfirmDialogOpen(false),
            });
        }
    };

    return (
        <>
            <AddAttendeeDialog
                open={isAddDialogOpen}
                onOpenChange={setIsAddDialogOpen}
                eventId={eventId}
                eventAttendees={data}
            />
            <ConfirmDialog
                open={isConfirmDialogOpen}
                onOpenChange={setIsConfirmDialogOpen}
                onConfirm={handleDeleteConfirm}
                title="Are you sure?"
                description={`This will remove ${selectedAttendee?.firstName} ${selectedAttendee?.lastName} from this event.`}
                isLoading={isRemovingAttendee}
            />
            <div className="flex w-full flex-col justify-start gap-4">
                <div className="flex items-center justify-between">
                    <Input
                        placeholder="Filter attendees by name..."
                        value={(table.getColumn("lastName")?.getFilterValue() as string) ?? ""}
                        onChange={(event) => table.getColumn("lastName")?.setFilterValue(event.target.value)}
                        className="h-9 max-w-sm"
                    />
                    <Button size="sm" className="h-9" onClick={() => setIsAddDialogOpen(true)}>
                        <IconPlus className="mr-2 h-4 w-4"/>
                        <span>Add Attendee</span>
                    </Button>
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
                                        Loading roster...
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
                                        No attendees have been added to this event yet.
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
