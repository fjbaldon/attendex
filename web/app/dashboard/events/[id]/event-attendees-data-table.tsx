"use client";

import * as React from "react";
import {ColumnDef} from "@tanstack/react-table";
import {IconPlus, IconTrash} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {AttendeeResponse} from "@/types";
import {useEventDetails} from "@/hooks/use-event-details";
import {ConfirmDialog} from "@/components/shared/confirm-dialog";
import {AddAttendeeDialog} from "./add-attendee-dialog";
import {DataTable} from "@/components/shared/data-table";

interface EventAttendeesDataTableProps {
    columns: ColumnDef<AttendeeResponse>[];
    data: AttendeeResponse[];
    isLoading: boolean;
    eventId: number;
    pageCount: number;
    pagination: { pageIndex: number; pageSize: number; };
    setPagination: (pagination: { pageIndex: number; pageSize: number; }) => void;
    // FIX: Add search props
    searchQuery: string;
    onSearchChange: (value: string) => void;
}

export function EventAttendeesDataTable({
                                            columns,
                                            data,
                                            isLoading,
                                            eventId,
                                            pageCount,
                                            pagination,
                                            setPagination,
                                            searchQuery,
                                            onSearchChange
                                        }: EventAttendeesDataTableProps) {
    const {removeAttendee, isRemovingAttendee, removeAttendees, isRemovingAttendees} = useEventDetails(eventId, pagination);

    const [isAddDialogOpen, setIsAddDialogOpen] = React.useState(false);
    const [isConfirmDialogOpen, setIsConfirmDialogOpen] = React.useState(false);
    const [selectedAttendee, setSelectedAttendee] = React.useState<AttendeeResponse | null>(null);

    // Bulk Selection State
    const [rowSelection, setRowSelection] = React.useState({});
    const [isBulkConfirmOpen, setIsBulkConfirmOpen] = React.useState(false);

    const handleDeleteConfirm = () => {
        if (selectedAttendee) {
            removeAttendee({eventId, attendeeId: selectedAttendee.id}, {
                onSuccess: () => setIsConfirmDialogOpen(false),
            });
        }
    };

    const handleBulkDeleteConfirm = () => {
        const selectedIds = Object.keys(rowSelection).map(index => data[Number(index)]?.id).filter(Boolean);
        if (selectedIds.length > 0) {
            removeAttendees({eventId, attendeeIds: selectedIds}, {
                onSuccess: () => {
                    setIsBulkConfirmOpen(false);
                    setRowSelection({});
                }
            });
        }
    };

    const selectedCount = Object.keys(rowSelection).length;

    const toolbar = (
        <div className="flex items-center justify-between">
            <div className="flex items-center gap-2 flex-1">
                <Input
                    placeholder="Filter attendees by name..."
                    value={searchQuery}
                    onChange={(event) => onSearchChange(event.target.value)}
                    className="h-9 max-w-sm"
                />
                {/* Bulk Delete Button */}
                {selectedCount > 0 && (
                    <Button
                        size="sm"
                        variant="destructive"
                        className="h-9 animate-in fade-in zoom-in-95"
                        onClick={() => setIsBulkConfirmOpen(true)}
                    >
                        <IconTrash className="mr-2 h-4 w-4"/>
                        Remove {selectedCount}
                    </Button>
                )}
            </div>
            <Button size="sm" className="h-9" onClick={() => setIsAddDialogOpen(true)}>
                <IconPlus className="mr-2 h-4 w-4"/>
                <span>Add Attendee</span>
            </Button>
        </div>
    );

    // FIX: Use data directly (Server filtered)
    return (
        <>
            <AddAttendeeDialog
                open={isAddDialogOpen}
                onOpenChange={setIsAddDialogOpen}
                eventId={eventId}
            />
            <ConfirmDialog
                open={isConfirmDialogOpen}
                onOpenChange={setIsConfirmDialogOpen}
                onConfirm={handleDeleteConfirm}
                title="Are you sure?"
                description={`This will remove ${selectedAttendee?.firstName} ${selectedAttendee?.lastName} from this event.`}
                isLoading={isRemovingAttendee}
            />
            <ConfirmDialog
                open={isBulkConfirmOpen}
                onOpenChange={setIsBulkConfirmOpen}
                onConfirm={handleBulkDeleteConfirm}
                title={`Remove ${selectedCount} Attendees?`}
                description="These attendees will be removed from the event roster."
                isLoading={isRemovingAttendees}
            />
            <DataTable
                columns={columns}
                data={data}
                isLoading={isLoading}
                pageCount={pageCount}
                pagination={pagination}
                setPagination={setPagination}
                toolbar={toolbar}
                state={{ rowSelection }}
                onRowSelectionChange={setRowSelection}
                meta={{
                    openDeleteDialog: (attendee: AttendeeResponse) => {
                        setSelectedAttendee(attendee);
                        setIsConfirmDialogOpen(true);
                    },
                }}
            />
        </>
    );
}
