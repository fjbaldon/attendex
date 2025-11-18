"use client";

import * as React from "react";
import {SiteHeader} from "@/components/layout/site-header";
import {useSystemAdmins} from "@/hooks/use-system-admins";
import {DataTable} from "@/components/shared/data-table";
import {columns} from "./columns";
import {Button} from "@/components/ui/button";
import {IconPlus} from "@tabler/icons-react";
import {AddAdminDialog} from "./add-admin-dialog";
import {ConfirmDialog} from "@/components/shared/confirm-dialog";
import {SystemAdmin} from "@/types";
import {ResetPasswordDialog} from "@/components/shared/reset-password-dialog";
import {useUserActions} from "@/hooks/use-user-actions";
import {Input} from "@/components/ui/input";

export default function AdminAdministratorsPage() {
    const [pagination, setPagination] = React.useState({pageIndex: 0, pageSize: 10});
    const [filter, setFilter] = React.useState("");

    const {adminsData, isLoadingAdmins, deleteAdmin, isDeletingAdmin} = useSystemAdmins(
        pagination.pageIndex,
        pagination.pageSize
    );
    const {resetPassword, isResettingPassword} = useUserActions();

    const [isAddDialogOpen, setIsAddDialogOpen] = React.useState(false);
    const [isConfirmDialogOpen, setIsConfirmDialogOpen] = React.useState(false);
    const [isResetDialogOpen, setIsResetDialogOpen] = React.useState(false);
    const [selectedAdmin, setSelectedAdmin] = React.useState<SystemAdmin | null>(null);

    const pageCount = adminsData?.totalPages ?? 0;

    const handleDeleteConfirm = () => {
        if (selectedAdmin) {
            deleteAdmin(selectedAdmin.id, {
                onSuccess: () => setIsConfirmDialogOpen(false),
            });
        }
    };

    const handleResetPassword = (values: { userId: number, newTemporaryPassword: string }) => {
        resetPassword(values, {
            onSuccess: () => setIsResetDialogOpen(false),
        });
    };

    const filteredData = React.useMemo(() => {
        const admins = adminsData?.content ?? [];
        return admins.filter(admin =>
            admin.email.toLowerCase().includes(filter.toLowerCase())
        );
    }, [adminsData, filter]);

    const toolbar = (
        <div className="flex items-center justify-between">
            <Input
                placeholder="Filter admins by email..."
                value={filter}
                onChange={(event) => setFilter(event.target.value)}
                className="h-9 max-w-sm"
            />
            <Button size="sm" className="h-9" onClick={() => setIsAddDialogOpen(true)}>
                <IconPlus className="mr-2 h-4 w-4"/>
                <span>Add Administrator</span>
            </Button>
        </div>
    );

    return (
        <>
            <SiteHeader title="Administrators"/>
            <main className="flex-1 p-4 lg:p-6">
                <div className="w-full max-w-6xl mx-auto space-y-6">
                    <AddAdminDialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}/>
                    <ConfirmDialog
                        open={isConfirmDialogOpen}
                        onOpenChange={setIsConfirmDialogOpen}
                        onConfirm={handleDeleteConfirm}
                        title="Are you sure?"
                        description={`This will permanently delete the admin account for ${selectedAdmin?.email}. This action cannot be undone.`}
                        isLoading={isDeletingAdmin}
                    />
                    <ResetPasswordDialog
                        open={isResetDialogOpen}
                        onOpenChange={setIsResetDialogOpen}
                        user={selectedAdmin}
                        onSubmit={handleResetPassword}
                        isLoading={isResettingPassword}
                    />
                    <DataTable
                        columns={columns}
                        data={filteredData}
                        isLoading={isLoadingAdmins}
                        pageCount={pageCount}
                        pagination={pagination}
                        setPagination={setPagination}
                        toolbar={toolbar}
                        meta={{
                            openDeleteDialog: (admin: SystemAdmin) => {
                                setSelectedAdmin(admin);
                                setIsConfirmDialogOpen(true);
                            },
                            openResetPasswordDialog: (admin: SystemAdmin) => {
                                setSelectedAdmin(admin);
                                setIsResetDialogOpen(true);
                            },
                        }}
                    />
                </div>
            </main>
        </>
    );
}
