"use client";

import * as React from "react";
import {ColumnDef} from "@tanstack/react-table";
import {IconPlus, IconTrash, IconUpload} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";
import {AttendeeResponse} from "@/types";
import {useAttendees} from "@/hooks/use-attendees";
import {ConfirmDialog} from "@/components/shared/confirm-dialog";
import {AttendeeDialog} from "./attendee-dialog";
import {AttendeeImportDialog} from "@/app/dashboard/attendees/attendee-import-dialog";
import {DataTable} from "@/components/shared/data-table";
import {useAttributes} from "@/hooks/use-attributes";
import {FilterToolbar} from "@/components/shared/filter-toolbar";
import {AttendeeDetailsSheet} from "./attendee-details-sheet";

interface AttendeesDataTableProps {
    columns: ColumnDef<AttendeeResponse>[];
    data: AttendeeResponse[];
    isLoading: boolean;
    pageCount: number;
    pagination: { pageIndex: number; pageSize: number; };
    setPagination: (pagination: { pageIndex: number; pageSize: number; }) => void;
    searchQuery: string;
    onSearchChange: (value: string) => void;
    activeFilters: Record<string, string>;
    onFiltersChange: (filters: Record<string, string>) => void;
}

export function AttendeesDataTable({
                                       columns,
                                       data,
                                       isLoading,
                                       pageCount,
                                       pagination,
                                       setPagination,
                                       searchQuery,
                                       onSearchChange,
                                       activeFilters,
                                       onFiltersChange,
                                   }: AttendeesDataTableProps) {
    // Use hook only for mutations now (delete)
    const {
        deleteAttendee,
        isDeletingAttendee,
        deleteAttendees,
        isDeletingAttendees,
    } = useAttendees(0, 0, "", {});

    // Attributes for the filter dropdown
    const {definitions: attributes} = useAttributes();

    const [isFormDialogOpen, setIsFormDialogOpen] = React.useState(false);
    const [isConfirmDialogOpen, setIsConfirmDialogOpen] = React.useState(false);
    const [isImportDialogOpen, setIsImportDialogOpen] = React.useState(false);
    const [isViewSheetOpen, setIsViewSheetOpen] = React.useState(false);

    const [rowSelection, setRowSelection] = React.useState({});
    const [isBulkDeleteOpen, setIsBulkDeleteOpen] = React.useState(false);

    // State for the selected attendee (for Edit, Delete, and View)
    const [selectedAttendee, setSelectedAttendee] = React.useState<AttendeeResponse | null>(null);
    // Separate state ID for the View Sheet to ensure data persistence during close animation
    const [viewAttendeeId, setViewAttendeeId] = React.useState<number | null>(null);

    const handleDeleteConfirm = () => {
        if (selectedAttendee) {
            deleteAttendee(selectedAttendee.id, {
                onSuccess: () => setIsConfirmDialogOpen(false),
            });
        }
    };

    const handleBulkDeleteConfirm = () => {
        const selectedIds = Object.keys(rowSelection)
            .map((index) => data[Number(index)]?.id)
            .filter((id) => id !== undefined);

        if (selectedIds.length > 0) {
            deleteAttendees(selectedIds, {
                onSuccess: () => {
                    setIsBulkDeleteOpen(false);
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
                searchPlaceholder="Search name..."
                activeFilters={activeFilters}
                onFiltersChange={onFiltersChange}
                attributes={attributes}
            >
                {/* Inject "Delete Selected" button here so it scrolls with the filters if needed */}
                {selectedCount > 0 && (
                    <div className="ml-auto sm:ml-2">
                        <Button
                            size="sm"
                            variant="destructive"
                            className="h-9 animate-in fade-in zoom-in-95"
                            onClick={() => setIsBulkDeleteOpen(true)}
                        >
                            <IconTrash className="mr-2 h-4 w-4"/>
                            Delete {selectedCount}
                        </Button>
                    </div>
                )}
            </FilterToolbar>

            <div className="flex items-center gap-2">
                <Button size="sm" variant="outline" className="h-9" onClick={() => setIsImportDialogOpen(true)}>
                    <IconUpload className="mr-2 h-4 w-4"/>
                    Import CSV
                </Button>
                <Button size="sm" className="h-9" onClick={() => {
                    setSelectedAttendee(null);
                    setIsFormDialogOpen(true);
                }}>
                    <IconPlus className="mr-2 h-4 w-4"/>
                    Add
                </Button>
            </div>
        </div>
    );

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

            <AttendeeDetailsSheet
                open={isViewSheetOpen}
                onOpenChange={setIsViewSheetOpen}
                attendeeId={viewAttendeeId}
            />

            <ConfirmDialog
                open={isConfirmDialogOpen}
                onOpenChange={setIsConfirmDialogOpen}
                onConfirm={handleDeleteConfirm}
                title="Remove Attendee?"
                description={`This will permanently remove ${selectedAttendee?.firstName} ${selectedAttendee?.lastName} from the organization.`}
                isLoading={isDeletingAttendee}
            />

            <ConfirmDialog
                open={isBulkDeleteOpen}
                onOpenChange={setIsBulkDeleteOpen}
                onConfirm={handleBulkDeleteConfirm}
                title={`Delete ${selectedCount} Attendees?`}
                description="This action cannot be undone. These attendees will be archived."
                isLoading={isDeletingAttendees}
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
                    openEditDialog: (attendee: AttendeeResponse) => {
                        setSelectedAttendee(attendee);
                        setIsFormDialogOpen(true);
                    },
                    openDeleteDialog: (attendee: AttendeeResponse) => {
                        setSelectedAttendee(attendee);
                        setIsConfirmDialogOpen(true);
                    },
                    openViewDialog: (attendee: AttendeeResponse) => {
                        setViewAttendeeId(attendee.id);
                        setIsViewSheetOpen(true);
                    },
                }}
            />
        </>
    );
}
