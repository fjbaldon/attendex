"use client";

import * as React from "react";
import {SiteHeader} from "@/components/layout/site-header";
import {useStewards} from "@/hooks/use-stewards";
import {DataTable} from "@/components/shared/data-table";
import {columns} from "./columns";
import {Button} from "@/components/ui/button";
import {IconPlus} from "@tabler/icons-react";
import {AddStewardDialog} from "./add-steward-dialog";
import {ConfirmDialog} from "@/components/shared/confirm-dialog";
import {Steward} from "@/types";
import {ResetPasswordDialog} from "@/components/shared/reset-password-dialog";
import {useUserActions} from "@/hooks/use-user-actions";
import {Input} from "@/components/ui/input";

export default function AdminStewardsPage() {
    const [pagination, setPagination] = React.useState({pageIndex: 0, pageSize: 10});
    const [filter, setFilter] = React.useState("");

    const {stewardsData, isLoadingStewards, deleteSteward, isDeletingSteward} = useStewards(
        pagination.pageIndex,
        pagination.pageSize
    );
    const {resetPassword, isResettingPassword} = useUserActions();

    const [isAddDialogOpen, setIsAddDialogOpen] = React.useState(false);
    const [isConfirmDialogOpen, setIsConfirmDialogOpen] = React.useState(false);
    const [isResetDialogOpen, setIsResetDialogOpen] = React.useState(false);
    const [selectedSteward, setSelectedSteward] = React.useState<Steward | null>(null);

    const pageCount = stewardsData?.totalPages ?? 0;

    const handleDeleteConfirm = () => {
        if (selectedSteward) {
            deleteSteward(selectedSteward.id, {
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
        const stewards = stewardsData?.content ?? [];
        return stewards.filter(steward =>
            steward.email.toLowerCase().includes(filter.toLowerCase())
        );
    }, [stewardsData, filter]);

    const toolbar = (
        <div className="flex items-center justify-between">
            <Input
                placeholder="Filter stewards by email..."
                value={filter}
                onChange={(event) => setFilter(event.target.value)}
                className="h-9 max-w-sm"
            />
            <Button size="sm" className="h-9" onClick={() => setIsAddDialogOpen(true)}>
                <IconPlus className="mr-2 h-4 w-4"/>
                <span>Add Steward</span>
            </Button>
        </div>
    );

    return (
        <>
            <SiteHeader title="Stewards"/>
            <main className="flex-1 p-4 lg:p-6">
                <div className="w-full max-w-6xl mx-auto space-y-6">
                    <AddStewardDialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}/>
                    <ConfirmDialog
                        open={isConfirmDialogOpen}
                        onOpenChange={setIsConfirmDialogOpen}
                        onConfirm={handleDeleteConfirm}
                        title="Are you sure?"
                        description={`This will permanently delete the steward account for ${selectedSteward?.email}. This action cannot be undone.`}
                        isLoading={isDeletingSteward}
                    />
                    <ResetPasswordDialog
                        open={isResetDialogOpen}
                        onOpenChange={setIsResetDialogOpen}
                        user={selectedSteward}
                        onSubmit={handleResetPassword}
                        isLoading={isResettingPassword}
                    />
                    <DataTable
                        columns={columns}
                        data={filteredData}
                        isLoading={isLoadingStewards}
                        pageCount={pageCount}
                        pagination={pagination}
                        setPagination={setPagination}
                        toolbar={toolbar}
                        meta={{
                            openDeleteDialog: (steward: Steward) => {
                                setSelectedSteward(steward);
                                setIsConfirmDialogOpen(true);
                            },
                            openResetPasswordDialog: (steward: Steward) => {
                                setSelectedSteward(steward);
                                setIsResetDialogOpen(true);
                            },
                        }}
                    />
                </div>
            </main>
        </>
    );
}
