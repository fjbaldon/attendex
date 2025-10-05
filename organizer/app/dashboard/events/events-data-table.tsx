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
    VisibilityState,
} from "@tanstack/react-table";
import {
    IconPlus,
} from "@tabler/icons-react";
import {z} from "zod";
import {eventSchema} from "@/lib/schemas";

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
import {EventResponse} from "@/types";
import {EventDialog} from "./event-dialog";
import {useEvents} from "@/hooks/use-events";
import {ConfirmDialog} from "@/components/shared/confirm-dialog";
import {DataTablePagination} from "@/components/shared/data-table-pagination";

interface EventsDataTableProps {
    columns: ColumnDef<EventResponse>[];
    data: EventResponse[];
    isLoading: boolean;
}

export function EventsDataTable({columns, data, isLoading}: EventsDataTableProps) {
    const [sorting, setSorting] = React.useState<SortingState>([]);
    const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([]);
    const [rowSelection, setRowSelection] = React.useState({});
    const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({});


    const [isFormDialogOpen, setIsFormDialogOpen] = React.useState(false);
    const [isConfirmDialogOpen, setIsConfirmDialogOpen] = React.useState(false);
    const [selectedEvent, setSelectedEvent] = React.useState<EventResponse | null>(null);

    const {
        createEvent,
        isCreatingEvent,
        updateEvent,
        isUpdatingEvent,
        deleteEvent,
        isDeletingEvent,
    } = useEvents();

    const table = useReactTable({
        data,
        columns,
        state: {
            sorting,
            columnFilters,
            rowSelection,
            columnVisibility,
        },
        getRowId: (row) => String(row.id),
        enableRowSelection: true,
        onRowSelectionChange: setRowSelection,
        onSortingChange: setSorting,
        onColumnFiltersChange: setColumnFilters,
        onColumnVisibilityChange: setColumnVisibility,
        getCoreRowModel: getCoreRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        getSortedRowModel: getSortedRowModel(),

        meta: {
            openEditDialog: (event: EventResponse) => {
                setSelectedEvent(event);
                setIsFormDialogOpen(true);
            },
            openDeleteDialog: (event: EventResponse) => {
                setSelectedEvent(event);
                setIsConfirmDialogOpen(true);
            },
        },
    });

    const handleOpenCreateDialog = () => {
        setSelectedEvent(null);
        setIsFormDialogOpen(true);
    };

    const handleFormSubmit = (values: z.infer<typeof eventSchema>) => {
        if (selectedEvent) {
            updateEvent({id: selectedEvent.id, data: values}, {
                onSuccess: () => setIsFormDialogOpen(false),
            });
        } else {
            createEvent(values, {
                onSuccess: () => setIsFormDialogOpen(false),
            });
        }
    };

    const handleDeleteConfirm = () => {
        if (selectedEvent) {
            deleteEvent(selectedEvent.id, {
                onSuccess: () => setIsConfirmDialogOpen(false),
            });
        }
    };

    return (
        <>
            <EventDialog
                open={isFormDialogOpen}
                onOpenChange={setIsFormDialogOpen}
                event={selectedEvent}
                onSubmit={handleFormSubmit}
                isLoading={isCreatingEvent || isUpdatingEvent}
            />
            <ConfirmDialog
                open={isConfirmDialogOpen}
                onOpenChange={setIsConfirmDialogOpen}
                onConfirm={handleDeleteConfirm}
                title="Are you sure?"
                description={`This will permanently delete the event "${selectedEvent?.eventName}". This action cannot be undone.`}
                isLoading={isDeletingEvent}
            />
            <div className="flex w-full flex-col justify-start gap-4">
                <div className="flex items-center justify-between">
                    <Input
                        placeholder="Filter events..."
                        value={(table.getColumn("eventName")?.getFilterValue() as string) ?? ""}
                        onChange={(event) =>
                            table.getColumn("eventName")?.setFilterValue(event.target.value)
                        }
                        className="h-9 max-w-sm"
                    />
                    <Button size="sm" className="h-9" onClick={handleOpenCreateDialog}>
                        <IconPlus className="mr-2 h-4 w-4"/>
                        <span>Add Event</span>
                    </Button>
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
                                        Loading events...
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
                                        No events found.
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
