"use client";

import * as React from "react";
import {columns} from "./columns";
import {useAdminOrganizations} from "@/hooks/use-admin-organizations";
import {DataTable} from "@/components/shared/data-table";
import {Organization} from "@/types";
import {StatusDialog} from "./status-dialog";
import {SubscriptionDialog} from "./subscription-dialog";
import {Input} from "@/components/ui/input";
import {SiteHeader} from "@/components/layout/site-header";

export default function AdminOrganizationsPage() {
    const [pagination, setPagination] = React.useState({pageIndex: 0, pageSize: 10});
    const [filter, setFilter] = React.useState("");

    const {organizationsData, isLoadingOrganizations} = useAdminOrganizations(
        pagination.pageIndex,
        pagination.pageSize
    );

    const [isStatusDialogOpen, setIsStatusDialogOpen] = React.useState(false);
    const [isSubDialogOpen, setIsSubDialogOpen] = React.useState(false);
    const [selectedOrg, setSelectedOrg] = React.useState<Organization | null>(null);

    const pageCount = organizationsData?.totalPages ?? 0;

    const openStatusDialog = (org: Organization) => {
        setSelectedOrg(org);
        setIsStatusDialogOpen(true);
    };

    const openSubscriptionDialog = (org: Organization) => {
        setSelectedOrg(org);
        setIsSubDialogOpen(true);
    };

    const filteredData = React.useMemo(() => {
        const organizations = organizationsData?.content ?? [];
        return organizations.filter(org =>
            org.name.toLowerCase().includes(filter.toLowerCase())
        );
    }, [organizationsData, filter]);

    const toolbar = (
        <div className="flex items-center justify-between">
            <Input
                placeholder="Filter organizations by name..."
                value={filter}
                onChange={(event) => setFilter(event.target.value)}
                className="h-9 max-w-sm"
            />
        </div>
    );

    return (
        <>
            <SiteHeader title="Organizations"/>
            <main className="flex-1 p-4 lg:p-6">
                <div className="w-full max-w-6xl mx-auto space-y-6">
                    <StatusDialog
                        open={isStatusDialogOpen}
                        onOpenChange={setIsStatusDialogOpen}
                        organization={selectedOrg}
                    />
                    <SubscriptionDialog
                        open={isSubDialogOpen}
                        onOpenChange={setIsSubDialogOpen}
                        organization={selectedOrg}
                    />
                    <DataTable
                        columns={columns}
                        data={filteredData}
                        isLoading={isLoadingOrganizations}
                        pageCount={pageCount}
                        pagination={pagination}
                        setPagination={setPagination}
                        toolbar={toolbar}
                        meta={{
                            openStatusDialog,
                            openSubscriptionDialog,
                        }}
                    />
                </div>
            </main>
        </>
    );
}
