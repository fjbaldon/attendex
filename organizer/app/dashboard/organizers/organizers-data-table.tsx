"use client";

import * as React from "react";
import {
    ColumnDef,
    flexRender,
    getCoreRowModel,
    getFilteredRowModel,
    getPaginationRowModel,
    getSortedRowModel,
    useReactTable,
    SortingState,
    ColumnFiltersState,
} from "@tanstack/react-table";
import {IconPlus} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {OrganizerResponse} from "@/types";
import {useOrganizers} from "@/hooks/use-organizers";
import {OrganizerDialog} from "./organizer-dialog";
import {ConfirmDialog} from "@/components/shared/confirm-dialog";
import {DataTablePagination} from "@/components/shared/data-table-pagination";
import {Input} from "@/components/ui/input";

interface OrganizersDataTableProps {
    columns: ColumnDef<OrganizerResponse>[];
    data: OrganizerResponse[];
    isLoading: boolean;
    pageCount: number;
    pagination: { pageIndex: number; pageSize: number; };
    setPagination: (pagination: { pageIndex: number; pageSize: number; }) => void;
}

export function OrganizersDataTable({
                                        columns,
                                        data,
                                        isLoading,
                                        pageCount,
                                        pagination,
                                        setPagination,
                                    }: OrganizersDataTableProps) {
    const {deleteOrganizer, isDeletingOrganizer} = useOrganizers();

    const [sorting, setSorting] = React.useState<SortingState>([]);
    const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([]);
    const [rowSelection, setRowSelection] = React.useState({});
    const [isFormDialogOpen, setIsFormDialogOpen] = React.useState(false);
    const [isConfirmDialogOpen, setIsConfirmDialogOpen] = React.useState(false);
    const [selectedOrganizer, setSelectedOrganizer] = React.useState<OrganizerResponse | null>(null);

    const table = useReactTable({
        data,
        columns,
        pageCount,
        state: {sorting, columnFilters, rowSelection, pagination},
        onSortingChange: setSorting,
        onColumnFiltersChange: setColumnFilters,
        onRowSelectionChange: setRowSelection,
        getCoreRowModel: getCoreRowModel(),
        getSortedRowModel: getSortedRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        onPaginationChange: (updater) => {
            if (typeof updater === 'function') setPagination(updater(pagination));
            else setPagination(updater);
        },
        manualPagination: true,
        meta: {
            openDeleteDialog: (organizer: OrganizerResponse) => {
                setSelectedOrganizer(organizer);
                setIsConfirmDialogOpen(true);
            },
        },
    });

    const handleOpenCreateDialog = () => {
        setSelectedOrganizer(null);
        setIsFormDialogOpen(true);
    };

    const handleDeleteConfirm = () => {
        if (selectedOrganizer) {
            deleteOrganizer(selectedOrganizer.id, {
                onSuccess: () => setIsConfirmDialogOpen(false),
            });
        }
    };

    return (
        <>
            <OrganizerDialog
                open={isFormDialogOpen}
                onOpenChange={setIsFormDialogOpen}
            />
            <ConfirmDialog
                open={isConfirmDialogOpen}
                onOpenChange={setIsConfirmDialogOpen}
                onConfirm={handleDeleteConfirm}
                title="Are you sure?"
                description={`This will permanently remove the organizer ${selectedOrganizer?.email} from the organization.`}
                isLoading={isDeletingOrganizer}
            />
            <div className="flex w-full flex-col justify-start gap-4">
                <div className="flex items-center justify-between">
                    <Input
                        placeholder="Filter organizers by email..."
                        value={(table.getColumn("email")?.getFilterValue() as string) ?? ""}
                        onChange={(event) => table.getColumn("email")?.setFilterValue(event.target.value)}
                        className="h-9 max-w-sm"
                    />
                    <Button size="sm" className="h-9" onClick={handleOpenCreateDialog}>
                        <IconPlus className="mr-2 h-4 w-4"/>
                        <span>Add Organizer</span>
                    </Button>
                </div>
                <div className="rounded-md border">
                    <Table>
                        <TableHeader className="bg-muted sticky top-0 z-10">
                            {table.getHeaderGroups().map((headerGroup) => (
                                <TableRow key={headerGroup.id}>
                                    {headerGroup.headers.map((header) => (
                                        <TableHead key={header.id} colSpan={header.colSpan}>
                                            {header.isPlaceholder ? null : flexRender(header.column.columnDef.header, header.getContext())}
                                        </TableHead>
                                    ))}
                                </TableRow>
                            ))}
                        </TableHeader>
                        <TableBody>
                            {isLoading ? (
                                <TableRow>
                                    <TableCell colSpan={columns.length} className="h-24 text-center">
                                        Loading organizers...
                                    </TableCell>
                                </TableRow>
                            ) : table.getRowModel().rows?.length ? (
                                table.getRowModel().rows.map((row) => (
                                    <TableRow key={row.id} data-state={row.getIsSelected() && "selected"}>
                                        {row.getVisibleCells().map((cell) => (
                                            <TableCell key={cell.id}>
                                                {flexRender(cell.column.columnDef.cell, cell.getContext())}
                                            </TableCell>
                                        ))}
                                    </TableRow>
                                ))
                            ) : (
                                <TableRow>
                                    <TableCell colSpan={columns.length} className="h-24 text-center">
                                        No organizers found.
                                    </TableCell>
                                </TableRow>
                            )}
                        </TableBody>
                    </Table>
                </div>
                <div className="pt-2">
                    <DataTablePagination table={table}/>
                </div>
            </div>
        </>
    );
}
