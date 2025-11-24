"use client";

import * as React from "react";
import {AppSidebar} from "@/components/layout/app-sidebar";
import {SiteHeader} from "@/components/layout/site-header";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {useOrphans} from "@/hooks/use-orphans";
import {columns} from "./columns";
import {DataTable} from "@/components/shared/data-table";
import {ConfirmDialog} from "@/components/shared/confirm-dialog";
import {OrphanedEntry} from "@/types";
import {IconAlertTriangle, IconRestore, IconTrash} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";
import {RecoverDialog} from "./recover-dialog";
import {BulkRecoverDialog} from "./bulk-recover-dialog"; // Import new component

export default function DataRecoveryPage() {
    const [{pageIndex, pageSize}, setPagination] = React.useState({
        pageIndex: 0,
        pageSize: 10,
    });

    const {
        orphansData,
        isLoading,
        deleteOrphan,
        isDeleting,
        deleteOrphans,
        isDeletingMultiple,
        recoverOrphan,
        isRecovering,
        recoverOrphans,         // New hook
        isRecoveringMultiple    // New state
    } = useOrphans(pageIndex, pageSize);

    const [selectedOrphan, setSelectedOrphan] = React.useState<OrphanedEntry | null>(null);
    const [isConfirmOpen, setIsConfirmOpen] = React.useState(false);
    const [isRecoverOpen, setIsRecoverOpen] = React.useState(false);

    // Bulk Selection State
    const [rowSelection, setRowSelection] = React.useState({});
    const [isBulkConfirmOpen, setIsBulkConfirmOpen] = React.useState(false);
    const [isBulkRecoverOpen, setIsBulkRecoverOpen] = React.useState(false); // New State

    const pageCount = orphansData?.totalPages ?? 0;
    const orphans = orphansData?.content ?? [];

    const handleDeleteConfirm = () => {
        if (selectedOrphan) {
            deleteOrphan(selectedOrphan.id, {
                onSuccess: () => setIsConfirmOpen(false)
            });
        }
    };

    const handleRecoverConfirm = (targetEventId: number) => {
        if (selectedOrphan) {
            recoverOrphan({
                orphanId: selectedOrphan.id,
                targetEventId
            }, {
                onSuccess: () => setIsRecoverOpen(false)
            });
        }
    };

    const handleBulkDeleteConfirm = () => {
        const selectedIds = Object.keys(rowSelection).map(index => orphans[Number(index)].id);
        if (selectedIds.length > 0) {
            deleteOrphans(selectedIds, {
                onSuccess: () => {
                    setIsBulkConfirmOpen(false);
                    setRowSelection({});
                }
            });
        }
    };

    // New Handler
    const handleBulkRecoverConfirm = (targetEventId: number) => {
        const selectedIds = Object.keys(rowSelection).map(index => orphans[Number(index)].id);
        if (selectedIds.length > 0) {
            recoverOrphans({
                orphanIds: selectedIds,
                targetEventId
            }, {
                onSuccess: () => {
                    setIsBulkRecoverOpen(false);
                    setRowSelection({});
                }
            });
        }
    };

    const selectedCount = Object.keys(rowSelection).length;

    const toolbar = (
        <div className="flex items-center justify-between w-full">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <IconAlertTriangle className="h-4 w-4 text-amber-500"/>
                <span>Entries from deleted or invalid events.</span>
            </div>
            {selectedCount > 0 && (
                <div className="flex items-center gap-2">
                    {/* NEW: Recover Selected Button */}
                    <Button
                        size="sm"
                        variant="outline"
                        onClick={() => setIsBulkRecoverOpen(true)}
                        className="gap-2"
                    >
                        <IconRestore className="w-4 h-4 text-primary"/>
                        Recover {selectedCount} Selected
                    </Button>

                    <Button
                        size="sm"
                        variant="destructive"
                        onClick={() => setIsBulkConfirmOpen(true)}
                    >
                        <IconTrash className="mr-2 h-4 w-4"/>
                        Dismiss {selectedCount}
                    </Button>
                </div>
            )}
        </div>
    );

    return (
        <SidebarProvider
            style={{
                "--sidebar-width": "calc(var(--spacing) * 72)",
                "--header-height": "calc(var(--spacing) * 12)",
            } as React.CSSProperties}
        >
            <AppSidebar variant="inset"/>
            <SidebarInset>
                <SiteHeader title="Recovery"/>
                <main className="flex-1 p-4 lg:p-6">
                    <div className="w-full max-w-6xl mx-auto space-y-6">
                        {/* Single Delete Dialog */}
                        <ConfirmDialog
                            open={isConfirmOpen}
                            onOpenChange={setIsConfirmOpen}
                            onConfirm={handleDeleteConfirm}
                            title="Dismiss Entry?"
                            description="This will permanently delete this quarantined record."
                            isLoading={isDeleting}
                        />

                        {/* Bulk Delete Dialog */}
                        <ConfirmDialog
                            open={isBulkConfirmOpen}
                            onOpenChange={setIsBulkConfirmOpen}
                            onConfirm={handleBulkDeleteConfirm}
                            title={`Dismiss ${selectedCount} Entries?`}
                            description="This action cannot be undone."
                            isLoading={isDeletingMultiple}
                        />

                        {/* Recover Dialog (Single) */}
                        <RecoverDialog
                            open={isRecoverOpen}
                            onOpenChange={setIsRecoverOpen}
                            orphan={selectedOrphan}
                            onConfirm={handleRecoverConfirm}
                            isLoading={isRecovering}
                        />

                        {/* Bulk Recover Dialog (New) */}
                        <BulkRecoverDialog
                            open={isBulkRecoverOpen}
                            onOpenChange={setIsBulkRecoverOpen}
                            count={selectedCount}
                            onConfirm={handleBulkRecoverConfirm}
                            isLoading={isRecoveringMultiple}
                        />

                        <DataTable
                            columns={columns}
                            data={orphans}
                            isLoading={isLoading}
                            pageCount={pageCount}
                            pagination={{pageIndex, pageSize}}
                            setPagination={setPagination}
                            toolbar={toolbar}
                            state={{ rowSelection }}
                            onRowSelectionChange={setRowSelection}
                            meta={{
                                openDeleteDialog: (orphan: OrphanedEntry) => {
                                    setSelectedOrphan(orphan);
                                    setIsConfirmOpen(true);
                                },
                                openRecoverDialog: (orphan: OrphanedEntry) => {
                                    setSelectedOrphan(orphan);
                                    setIsRecoverOpen(true);
                                }
                            }}
                        />
                    </div>
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}
