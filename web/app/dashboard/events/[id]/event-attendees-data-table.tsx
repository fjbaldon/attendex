"use client";

import * as React from "react";
import {ColumnDef} from "@tanstack/react-table";
import {IconPlus} from "@tabler/icons-react";
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
}

export function EventAttendeesDataTable({
                                            columns,
                                            data,
                                            isLoading,
                                            eventId,
                                            pageCount,
                                            pagination,
                                            setPagination
                                        }: EventAttendeesDataTableProps) {
    const {removeAttendee, isRemovingAttendee} = useEventDetails(eventId, pagination);

    const [isAddDialogOpen, setIsAddDialogOpen] = React.useState(false);
    const [isConfirmDialogOpen, setIsConfirmDialogOpen] = React.useState(false);
    const [selectedAttendee, setSelectedAttendee] = React.useState<AttendeeResponse | null>(null);
    const [filter, setFilter] = React.useState("");

    const handleDeleteConfirm = () => {
        if (selectedAttendee) {
            removeAttendee({eventId, attendeeId: selectedAttendee.id}, {
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
            <Button size="sm" className="h-9" onClick={() => setIsAddDialogOpen(true)}>
                <IconPlus className="mr-2 h-4 w-4"/>
                <span>Add Attendee</span>
            </Button>
        </div>
    );

    const filteredData = React.useMemo(() =>
        data.filter(attendee =>
            `${attendee.firstName} ${attendee.lastName}`.toLowerCase().includes(filter.toLowerCase())
        ), [data, filter]);

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
            <DataTable
                columns={columns}
                data={filteredData}
                isLoading={isLoading}
                pageCount={pageCount}
                pagination={pagination}
                setPagination={setPagination}
                toolbar={toolbar}
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
