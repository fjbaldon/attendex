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
import {useDebounce} from "@uidotdev/usehooks";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Button} from "@/components/ui/button";
import {IconFilterX} from "@tabler/icons-react";

export default function AdminOrganizationsPage() {
    const [pagination, setPagination] = React.useState({pageIndex: 0, pageSize: 10});

    const [searchQuery, setSearchQuery] = React.useState("");
    const [statusFilter, setStatusFilter] = React.useState("ALL");
    const [subFilter, setSubFilter] = React.useState("ALL");

    const debouncedQuery = useDebounce(searchQuery, 500);

    // Reset pagination on filter change
    React.useEffect(() => {
        setPagination(prev => ({ ...prev, pageIndex: 0 }));
    }, [debouncedQuery, statusFilter, subFilter]);

    const {organizationsData, isLoadingOrganizations} = useAdminOrganizations(
        pagination.pageIndex,
        pagination.pageSize,
        debouncedQuery,
        statusFilter,
        subFilter
    );

    const [isStatusDialogOpen, setIsStatusDialogOpen] = React.useState(false);
    const [isSubDialogOpen, setIsSubDialogOpen] = React.useState(false);
    const [selectedOrg, setSelectedOrg] = React.useState<Organization | null>(null);

    const pageCount = organizationsData?.totalPages ?? 0;
    const organizations = organizationsData?.content ?? [];

    const openStatusDialog = (org: Organization) => {
        setSelectedOrg(org);
        setIsStatusDialogOpen(true);
    };

    const openSubscriptionDialog = (org: Organization) => {
        setSelectedOrg(org);
        setIsSubDialogOpen(true);
    };

    const clearFilters = () => {
        setSearchQuery("");
        setStatusFilter("ALL");
        setSubFilter("ALL");
    };

    const hasFilters = searchQuery || statusFilter !== "ALL" || subFilter !== "ALL";

    const toolbar = (
        <div className="flex flex-col sm:flex-row gap-3 items-start sm:items-center w-full">
            <Input
                placeholder="Search organizations..."
                value={searchQuery}
                onChange={(event) => setSearchQuery(event.target.value)}
                className="h-9 w-full sm:w-[250px]"
            />

            <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger className="h-9 w-full sm:w-[140px]">
                    <SelectValue placeholder="Status" />
                </SelectTrigger>
                <SelectContent>
                    <SelectItem value="ALL">All Status</SelectItem>
                    <SelectItem value="ACTIVE">Active</SelectItem>
                    <SelectItem value="INACTIVE">Inactive</SelectItem>
                    <SelectItem value="SUSPENDED">Suspended</SelectItem>
                </SelectContent>
            </Select>

            <Select value={subFilter} onValueChange={setSubFilter}>
                <SelectTrigger className="h-9 w-full sm:w-[140px]">
                    <SelectValue placeholder="Subscription" />
                </SelectTrigger>
                <SelectContent>
                    <SelectItem value="ALL">All Plans</SelectItem>
                    <SelectItem value="TRIAL">Trial</SelectItem>
                    <SelectItem value="ANNUAL">Annual</SelectItem>
                    <SelectItem value="LIFETIME">Lifetime</SelectItem>
                </SelectContent>
            </Select>

            {hasFilters && (
                <Button
                    variant="ghost"
                    size="sm"
                    onClick={clearFilters}
                    className="h-9 px-2 lg:px-3 text-muted-foreground"
                >
                    <IconFilterX className="mr-2 h-4 w-4" />
                    Clear
                </Button>
            )}
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
                        data={organizations}
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
