"use client";

import * as React from "react";
import {ColumnDef} from "@tanstack/react-table";
import {IconPlus, IconTrash} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";
import {AttendeeResponse, Attribute} from "@/types";
import {useEventDetails} from "@/hooks/use-event-details";
import {ConfirmDialog} from "@/components/shared/confirm-dialog";
import {AddAttendeeDialog} from "./add-attendee-dialog";
import {DataTable} from "@/components/shared/data-table";
import {FilterToolbar} from "@/components/shared/filter-toolbar";

interface EventAttendeesDataTableProps {
    columns: ColumnDef<AttendeeResponse>[];
    data: AttendeeResponse[];
    isLoading: boolean;
    eventId: number;
    pageCount: number;
    pagination: { pageIndex: number; pageSize: number; };
    // FIX: Updated type to allow functional updates (prev => ...)
    setPagination: React.Dispatch<React.SetStateAction<{ pageIndex: number; pageSize: number; }>>;
    searchQuery: string;
    onSearchChange: (value: string) => void;
    activeFilters: Record<string, string>;
    onFiltersChange: (filters: Record<string, string>) => void;
    attributes: Attribute[];
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
                                            onSearchChange,
                                            activeFilters,
                                            onFiltersChange,
                                            attributes
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
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between w-full">
            <FilterToolbar
                searchQuery={searchQuery}
                onSearchChange={onSearchChange}
                searchPlaceholder="Search roster..."
                activeFilters={activeFilters}
                onFiltersChange={(filters) => {
                    onFiltersChange(filters);
                    // This functional update caused the TS error before the type fix
                    setPagination((prev) => ({...prev, pageIndex: 0}));
                }}
                attributes={attributes}
            >
                {/* Bulk Delete Button injected into toolbar */}
                {selectedCount > 0 && (
                    <Button
                        size="sm"
                        variant="destructive"
                        className="h-9 animate-in fade-in zoom-in-95 ml-2"
                        onClick={() => setIsBulkConfirmOpen(true)}
                    >
                        <IconTrash className="mr-2 h-4 w-4"/>
                        Remove {selectedCount}
                    </Button>
                )}
            </FilterToolbar>

            <Button size="sm" className="h-9 shrink-0" onClick={() => setIsAddDialogOpen(true)}>
                <IconPlus className="mr-2 h-4 w-4"/>
                <span>Add Attendee</span>
            </Button>
        </div>
    );

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
