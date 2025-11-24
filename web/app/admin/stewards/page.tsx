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
import {Input} from "@/components/ui/input";
import {useDebounce} from "@uidotdev/usehooks";

export default function AdminStewardsPage() {
    const [pagination, setPagination] = React.useState({pageIndex: 0, pageSize: 10});
    const [searchQuery, setSearchQuery] = React.useState("");
    const debouncedQuery = useDebounce(searchQuery, 500);

    // Reset pagination on search
    React.useEffect(() => {
        setPagination(prev => ({ ...prev, pageIndex: 0 }));
    }, [debouncedQuery]);

    const {
        stewardsData,
        isLoadingStewards,
        deleteSteward,
        isDeletingSteward,
        resetStewardPassword,
        isResettingStewardPassword
    } = useStewards(pagination.pageIndex, pagination.pageSize, debouncedQuery);

    const [isAddDialogOpen, setIsAddDialogOpen] = React.useState(false);
    const [isConfirmDialogOpen, setIsConfirmDialogOpen] = React.useState(false);
    const [isResetDialogOpen, setIsResetDialogOpen] = React.useState(false);
    const [selectedSteward, setSelectedSteward] = React.useState<Steward | null>(null);

    const pageCount = stewardsData?.totalPages ?? 0;
    const stewards = stewardsData?.content ?? [];

    const handleDeleteConfirm = () => {
        if (selectedSteward) {
            deleteSteward(selectedSteward.id, {
                onSuccess: () => setIsConfirmDialogOpen(false),
            });
        }
    };

    const handleResetPassword = (values: { userId: number, newTemporaryPassword: string }) => {
        resetStewardPassword({
            id: values.userId,
            newPassword: values.newTemporaryPassword
        }, {
            onSuccess: () => setIsResetDialogOpen(false),
        });
    };

    const toolbar = (
        <div className="flex items-center justify-between">
            <Input
                placeholder="Search stewards by email..."
                value={searchQuery}
                onChange={(event) => setSearchQuery(event.target.value)}
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
                        title="Remove Steward?"
                        description={`This will permanently remove the steward account for ${selectedSteward?.email}.`}
                        isLoading={isDeletingSteward}
                    />
                    <ResetPasswordDialog
                        open={isResetDialogOpen}
                        onOpenChange={setIsResetDialogOpen}
                        user={selectedSteward}
                        onSubmit={handleResetPassword}
                        isLoading={isResettingStewardPassword}
                    />
                    <DataTable
                        columns={columns}
                        data={stewards}
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
