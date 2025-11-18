"use client";

import * as React from "react";
import {ColumnDef} from "@tanstack/react-table";
import {IconPlus, IconUpload} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";
import {AttendeeResponse} from "@/types";
import {useAttendees} from "@/hooks/use-attendees";
import {ConfirmDialog} from "@/components/shared/confirm-dialog";
import {Input} from "@/components/ui/input";
import {AttendeeDialog} from "./attendee-dialog";
import {AttendeeImportDialog} from "@/app/dashboard/attendees/attendee-import-dialog";
import {DataTable} from "@/components/shared/data-table";

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

    const [isFormDialogOpen, setIsFormDialogOpen] = React.useState(false);
    const [isConfirmDialogOpen, setIsConfirmDialogOpen] = React.useState(false);
    const [isImportDialogOpen, setIsImportDialogOpen] = React.useState(false);
    const [selectedAttendee, setSelectedAttendee] = React.useState<AttendeeResponse | null>(null);
    const [filter, setFilter] = React.useState("");

    const handleDeleteConfirm = () => {
        if (selectedAttendee) {
            deleteAttendee(selectedAttendee.id, {
                onSuccess: () => setIsConfirmDialogOpen(false),
            });
        }
    };

    const toolbar = (
        <div className="flex items-center justify-between">
            <Input
                placeholder="Filter attendees by name..."
                value={filter}
                onChange={(event) => setFilter(event.target.value)}
                className="h-9 max-w-sm"
            />
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
                    <span>Add Attendee</span>
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
            <ConfirmDialog
                open={isConfirmDialogOpen}
                onOpenChange={setIsConfirmDialogOpen}
                onConfirm={handleDeleteConfirm}
                title="Are you sure?"
                description={`This will permanently delete ${selectedAttendee?.firstName} ${selectedAttendee?.lastName}. This action cannot be undone.`}
                isLoading={isDeletingAttendee}
            />
            <DataTable
                columns={columns}
                data={data}
                isLoading={isLoading}
                pageCount={pageCount}
                pagination={pagination}
                setPagination={setPagination}
                toolbar={toolbar}
                meta={{
                    openEditDialog: (attendee: AttendeeResponse) => {
                        setSelectedAttendee(attendee);
                        setIsFormDialogOpen(true);
                    },
                    openDeleteDialog: (attendee: AttendeeResponse) => {
                        setSelectedAttendee(attendee);
                        setIsConfirmDialogOpen(true);
                    },
                }}
            />
        </>
    );
}
