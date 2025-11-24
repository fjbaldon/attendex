"use client";

import * as React from "react";
import {IconPlus} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";
import {ScannerResponse} from "@/types";
import {useScanners} from "@/hooks/use-scanners";
import {ScannerDialog} from "./scanner-dialog";
import {ConfirmDialog} from "@/components/shared/confirm-dialog";
import {Input} from "@/components/ui/input";
import {useUserActions} from "@/hooks/use-user-actions";
import {ResetPasswordDialog} from "@/components/shared/reset-password-dialog";
import {DataTable} from "@/components/shared/data-table";
import {getColumns} from "./columns";

interface ScannersDataTableProps {
    data: ScannerResponse[];
    isLoading: boolean;
    pageCount: number;
    pagination: { pageIndex: number; pageSize: number; };
    setPagination: (pagination: { pageIndex: number; pageSize: number; }) => void;
    // FIX: Add search props
    onSearchChange: (value: string) => void;
    searchValue: string;
}

export function ScannersDataTable({
                                      data,
                                      isLoading,
                                      pageCount,
                                      pagination,
                                      setPagination,
                                      onSearchChange,
                                      searchValue
                                  }: ScannersDataTableProps) {
    const { deleteScanner, isDeletingScanner, toggleScannerStatus } = useScanners();
    const { resetPassword, isResettingPassword } = useUserActions();

    const [isFormDialogOpen, setIsFormDialogOpen] = React.useState(false);
    const [isConfirmDialogOpen, setIsConfirmDialogOpen] = React.useState(false);
    const [isResetDialogOpen, setIsResetDialogOpen] = React.useState(false);
    const [selectedScanner, setSelectedScanner] = React.useState<ScannerResponse | null>(null);

    const columns = React.useMemo(
        () => getColumns(toggleScannerStatus),
        [toggleScannerStatus]
    );

    const handleDeleteConfirm = () => {
        if (selectedScanner) {
            deleteScanner(selectedScanner.id, {
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
                placeholder="Filter scanners by email..."
                value={searchValue}
                onChange={(event) => onSearchChange(event.target.value)}
                className="h-9 max-w-sm"
            />
            <Button size="sm" className="h-9" onClick={() => setIsFormDialogOpen(true)}>
                <IconPlus className="mr-2 h-4 w-4"/>
                <span>Add Scanner</span>
            </Button>
        </div>
    );

    // FIX: Removed filteredData useMemo. Using 'data' directly.

    return (
        <>
            <ScannerDialog
                open={isFormDialogOpen}
                onOpenChange={setIsFormDialogOpen}
            />
            <ResetPasswordDialog
                open={isResetDialogOpen}
                onOpenChange={setIsResetDialogOpen}
                user={selectedScanner}
                onSubmit={handleResetPasswordSubmit}
                isLoading={isResettingPassword}
            />
            <ConfirmDialog
                open={isConfirmDialogOpen}
                onOpenChange={setIsConfirmDialogOpen}
                onConfirm={handleDeleteConfirm}
                title="Are you sure?"
                description={`This will permanently remove the scanner account for ${selectedScanner?.email}.`}
                isLoading={isDeletingScanner}
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
                    openDeleteDialog: (scanner: ScannerResponse) => {
                        setSelectedScanner(scanner);
                        setIsConfirmDialogOpen(true);
                    },
                    openResetPasswordDialog: (scanner: ScannerResponse) => {
                        setSelectedScanner(scanner);
                        setIsResetDialogOpen(true);
                    },
                }}
            />
        </>
    );
}
