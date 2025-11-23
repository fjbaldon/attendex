"use client";

import * as React from "react";
import {useEffect, useMemo, useState} from "react";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle
} from "@/components/ui/dialog";
import {useAttendees} from "@/hooks/use-attendees";
import {useEventDetails} from "@/hooks/use-event-details";
import {AttendeeResponse} from "@/types";
import {Button} from "@/components/ui/button";
import {useDebounce} from "@uidotdev/usehooks";
import {flexRender, getCoreRowModel, useReactTable} from "@tanstack/react-table";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {Input} from "@/components/ui/input";
import {DataTablePagination} from "@/components/shared/data-table-pagination";
import {getColumns} from "./add-attendees-columns";
import {useAttributes} from "@/hooks/use-attributes";
import {toast} from "sonner";
import {useQuery} from "@tanstack/react-query";
import api from "@/lib/api";

interface AddAttendeeDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    eventId: number;
}

// Helper to get current roster IDs to disable checkboxes
const useEventRosterIds = (eventId: number) => {
    return useQuery<Set<number>>({
        queryKey: ["eventDetails", eventId, "rosterIds"],
        queryFn: async () => {
            const response = await api.get(`/api/v1/events/${eventId}/roster?size=10000`);
            return new Set(response.data.content.map((a: AttendeeResponse) => a.id));
        },
        enabled: !!eventId,
    });
};

export function AddAttendeeDialog({open, onOpenChange, eventId}: AddAttendeeDialogProps) {
    // 1. Search State
    const [searchQuery, setSearchQuery] = useState("");
    const debouncedSearchQuery = useDebounce(searchQuery, 500);

    // 2. Pagination State
    const [pagination, setPagination] = useState({pageIndex: 0, pageSize: 10});

    const [rowSelection, setRowSelection] = useState({});

    // 3. Data Fetching (Server-Side)
    const {attendeesData, isLoadingAttendees, refetch: refetchAttendees} =
        useAttendees(pagination.pageIndex, pagination.pageSize, debouncedSearchQuery);

    const {data: rosterIds, refetch: refetchRoster} = useEventRosterIds(eventId);

    const {addAttendee, isAddingAttendee} = useEventDetails(eventId, {pageIndex: 0, pageSize: 10});
    const {definitions: attributes, isLoading: isLoadingAttributes} = useAttributes();

    // 4. Setup Columns
    const columns = useMemo(() => getColumns(attributes), [attributes]);

    // 5. Filter out attendees already in roster (Client-side check of Server-side results)
    // Note: In a truly massive system, the backend search should exclude these.
    // For now, we disable selection if they are found in the rosterIds set.
    const attendees = attendeesData?.content ?? [];

    const table = useReactTable({
        data: attendees,
        columns,
        pageCount: attendeesData?.totalPages ?? -1, // -1 indicates server-side unknown, or use totalPages
        state: {
            rowSelection,
            pagination,
        },
        getRowId: (row) => String(row.id),
        enableRowSelection: (row) => !rosterIds?.has(row.original.id), // Disable if already added
        onRowSelectionChange: setRowSelection,
        onPaginationChange: setPagination, // Handle page changes
        manualPagination: true, // Enable server-side pagination
        getCoreRowModel: getCoreRowModel(),
    });

    useEffect(() => {
        if (open) {
            setSearchQuery("");
            setRowSelection({});
            setPagination({pageIndex: 0, pageSize: 10});
            void refetchAttendees();
            void refetchRoster();
        }
    }, [open, refetchAttendees, refetchRoster]);

    const handleAddSelected = async () => {
        const selectedIds = Object.keys(rowSelection).map(Number);
        if (selectedIds.length === 0) return;

        let successCount = 0;
        for (const attendeeId of selectedIds) {
            try {
                await new Promise<void>((resolve, reject) => {
                    addAttendee({eventId, attendeeId}, {onSuccess: () => resolve(), onError: (e) => reject(e)});
                });
                successCount++;
            } catch {
                toast.error(`Failed to add attendee ID ${attendeeId}`);
                break;
            }
        }

        if (successCount > 0) {
            toast.success(`${successCount} attendee(s) added!`);
            // Refresh roster IDs to update disabled state
            void refetchRoster();
            setRowSelection({});
        }
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-4xl max-h-[90vh] flex flex-col">
                <DialogHeader>
                    <DialogTitle>Add Attendees to Roster</DialogTitle>
                    <DialogDescription>
                        Search and select attendees to add to this event.
                    </DialogDescription>
                </DialogHeader>

                <div className="space-y-4 flex-grow flex flex-col min-h-0">
                    <div className="flex flex-col sm:flex-row gap-2">
                        <Input
                            placeholder="Search by name or identity..."
                            value={searchQuery}
                            onChange={(e) => {
                                setSearchQuery(e.target.value);
                                setPagination(prev => ({...prev, pageIndex: 0})); // Reset page on search
                            }}
                            className="h-9 max-w-sm"
                        />
                    </div>

                    <div className="rounded-md border flex-grow overflow-y-auto relative">
                        <Table>
                            <TableHeader className="bg-muted sticky top-0 z-10">
                                {table.getHeaderGroups().map(headerGroup => (
                                    <TableRow key={headerGroup.id}>
                                        {headerGroup.headers.map(header => (
                                            <TableHead key={header.id}>
                                                {flexRender(header.column.columnDef.header, header.getContext())}
                                            </TableHead>
                                        ))}
                                    </TableRow>
                                ))}
                            </TableHeader>
                            <TableBody>
                                {isLoadingAttendees || isLoadingAttributes ? (
                                    <TableRow>
                                        <TableCell colSpan={columns.length} className="h-24 text-center">
                                            Loading...
                                        </TableCell>
                                    </TableRow>
                                ) : table.getRowModel().rows.length > 0 ? (
                                    table.getRowModel().rows.map(row => (
                                        <TableRow key={row.id} data-state={row.getIsSelected() && "selected"}
                                                  className={rosterIds?.has(row.original.id) ? "opacity-50 bg-muted/50" : ""}>
                                            {row.getVisibleCells().map(cell => (
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
                    {/* Use the existing Pagination Component, passing the table instance which now has manual pagination config */}
                    <DataTablePagination table={table}/>
                </div>
                <DialogFooter className="pt-4 border-t">
                    <Button variant="outline" onClick={() => onOpenChange(false)}>Close</Button>
                    <Button onClick={handleAddSelected}
                            disabled={isAddingAttendee || Object.keys(rowSelection).length === 0}>
                        {isAddingAttendee ? "Adding..." : `Add ${Object.keys(rowSelection).length} Selected`}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
