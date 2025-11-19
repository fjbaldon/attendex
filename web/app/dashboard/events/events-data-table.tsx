"use client";

import * as React from "react";
import {ColumnDef} from "@tanstack/react-table";
import {z} from "zod";
import {eventSchema} from "@/lib/schemas";
import {IconPlus} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {EventResponse} from "@/types";
import {EventDialog} from "./event-dialog";
import {useEvents} from "@/hooks/use-events";
import {ConfirmDialog} from "@/components/shared/confirm-dialog";
import {DataTable} from "@/components/shared/data-table";

interface EventsDataTableProps {
    columns: ColumnDef<EventResponse>[];
    data: EventResponse[];
    isLoading: boolean;
    pageCount: number;
    pagination: { pageIndex: number; pageSize: number; };
    setPagination: (pagination: { pageIndex: number; pageSize: number; }) => void;
    // New props for Server-Side Search
    onSearchChange: (value: string) => void;
    searchValue: string;
}

export function EventsDataTable({
                                    columns,
                                    data,
                                    isLoading,
                                    pageCount,
                                    pagination,
                                    setPagination,
                                    onSearchChange,
                                    searchValue
                                }: EventsDataTableProps) {
    const {createEvent, isCreatingEvent, updateEvent, isUpdatingEvent, deleteEvent, isDeletingEvent} = useEvents();

    const [isFormDialogOpen, setIsFormDialogOpen] = React.useState(false);
    const [isConfirmDialogOpen, setIsConfirmDialogOpen] = React.useState(false);
    const [selectedEvent, setSelectedEvent] = React.useState<EventResponse | null>(null);

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

    const toolbar = (
        <div className="flex items-center justify-between">
            <Input
                placeholder="Filter events..."
                value={searchValue}
                onChange={(event) => onSearchChange(event.target.value)}
                className="h-9 max-w-sm"
            />
            <Button size="sm" className="h-9" onClick={() => {
                setSelectedEvent(null);
                setIsFormDialogOpen(true);
            }}>
                <IconPlus className="mr-2 h-4 w-4"/>
                <span>Add Event</span>
            </Button>
        </div>
    );

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
                description={`This will permanently delete the event "${selectedEvent?.name}". This action cannot be undone.`}
                isLoading={isDeletingEvent}
            />
            <DataTable
                columns={columns}
                data={data} // Pass data directly, filtering happens on backend
                isLoading={isLoading}
                pageCount={pageCount}
                pagination={pagination}
                setPagination={setPagination}
                toolbar={toolbar}
                meta={{
                    openEditDialog: (event: EventResponse) => {
                        setSelectedEvent(event);
                        setIsFormDialogOpen(true);
                    },
                    openDeleteDialog: (event: EventResponse) => {
                        setSelectedEvent(event);
                        setIsConfirmDialogOpen(true);
                    },
                }}
            />
        </>
    );
}
