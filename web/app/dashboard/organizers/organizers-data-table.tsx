"use client";

import * as React from "react";
import {ColumnDef} from "@tanstack/react-table";
import {IconPlus} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";
import {OrganizerResponse} from "@/types";
import {useOrganizers} from "@/hooks/use-organizers";
import {OrganizerDialog} from "./organizer-dialog";
import {ConfirmDialog} from "@/components/shared/confirm-dialog";
import {Input} from "@/components/ui/input";
import {useUserActions} from "@/hooks/use-user-actions";
import {ResetPasswordDialog} from "@/components/shared/reset-password-dialog";
import {DataTable} from "@/components/shared/data-table";

interface OrganizersDataTableProps {
    columns: ColumnDef<OrganizerResponse>[];
    data: OrganizerResponse[];
    isLoading: boolean;
    pageCount: number;
    pagination: { pageIndex: number; pageSize: number; };
    setPagination: (pagination: { pageIndex: number; pageSize: number; }) => void;
    // FIX: Add search props
    onSearchChange: (value: string) => void;
    searchValue: string;
}

export function OrganizersDataTable({
                                        columns,
                                        data,
                                        isLoading,
                                        pageCount,
                                        pagination,
                                        setPagination,
                                        onSearchChange,
                                        searchValue
                                    }: OrganizersDataTableProps) {
    // No page/size needed here for mutations
    const {deleteOrganizer, isDeletingOrganizer} = useOrganizers();
    const {resetPassword, isResettingPassword} = useUserActions();

    const [isFormDialogOpen, setIsFormDialogOpen] = React.useState(false);
    const [isConfirmDialogOpen, setIsConfirmDialogOpen] = React.useState(false);
    const [isResetDialogOpen, setIsResetDialogOpen] = React.useState(false);
    const [selectedOrganizer, setSelectedOrganizer] = React.useState<OrganizerResponse | null>(null);

    const handleDeleteConfirm = () => {
        if (selectedOrganizer) {
            deleteOrganizer(selectedOrganizer.id, {
                onSuccess: () => setIsConfirmDialogOpen(false),
            });
        }
    };

    const handleResetPasswordSubmit = (values: { userId: number, newTemporaryPassword: string }) => {
        resetPassword(values, {
            onSuccess: () => setIsResetDialogOpen(false),
        });
    };

    const toolbar = (
        <div className="flex items-center justify-between">
            <Input
                placeholder="Filter organizers by email..."
                value={searchValue}
                onChange={(event) => onSearchChange(event.target.value)}
                className="h-9 max-w-sm"
            />
            <Button size="sm" className="h-9" onClick={() => setIsFormDialogOpen(true)}>
                <IconPlus className="mr-2 h-4 w-4"/>
                <span>Add Organizer</span>
            </Button>
        </div>
    );

    // FIX: Removed client-side filtering. Data is already filtered by server.

    return (
        <>
            <OrganizerDialog
                open={isFormDialogOpen}
                onOpenChange={setIsFormDialogOpen}
            />
            <ResetPasswordDialog
                open={isResetDialogOpen}
                onOpenChange={setIsResetDialogOpen}
                user={selectedOrganizer}
                onSubmit={handleResetPasswordSubmit}
                isLoading={isResettingPassword}
            />
            <ConfirmDialog
                open={isConfirmDialogOpen}
                onOpenChange={setIsConfirmDialogOpen}
                onConfirm={handleDeleteConfirm}
                title="Are you sure?"
                description={`This will permanently remove the organizer ${selectedOrganizer?.email} from the organization.`}
                isLoading={isDeletingOrganizer}
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
                    openDeleteDialog: (organizer: OrganizerResponse) => {
                        setSelectedOrganizer(organizer);
                        setIsConfirmDialogOpen(true);
                    },
                    openResetPasswordDialog: (organizer: OrganizerResponse) => {
                        setSelectedOrganizer(organizer);
                        setIsResetDialogOpen(true);
                    },
                }}
            />
        </>
    );
}
