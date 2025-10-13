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
import {ScannerResponse} from "@/types";
import {useScanners} from "@/hooks/use-scanners";
import {ScannerDialog} from "./scanner-dialog";
import {ConfirmDialog} from "@/components/shared/confirm-dialog";
import {DataTablePagination} from "@/components/shared/data-table-pagination";
import {Input} from "@/components/ui/input";

interface ScannersDataTableProps {
    columns: ColumnDef<ScannerResponse>[];
    data: ScannerResponse[];
    isLoading: boolean;
}

export function ScannersDataTable({columns, data, isLoading}: ScannersDataTableProps) {
    const {deleteScanner, isDeletingScanner} = useScanners();

    const [sorting, setSorting] = React.useState<SortingState>([]);
    const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([]);
    const [rowSelection, setRowSelection] = React.useState({});
    const [isFormDialogOpen, setIsFormDialogOpen] = React.useState(false);
    const [isConfirmDialogOpen, setIsConfirmDialogOpen] = React.useState(false);
    const [selectedScanner, setSelectedScanner] = React.useState<ScannerResponse | null>(null);

    const table = useReactTable({
        data,
        columns,
        state: {sorting, columnFilters, rowSelection},
        onSortingChange: setSorting,
        onColumnFiltersChange: setColumnFilters,
        onRowSelectionChange: setRowSelection,
        getCoreRowModel: getCoreRowModel(),
        getSortedRowModel: getSortedRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        meta: {
            openDeleteDialog: (scanner: ScannerResponse) => {
                setSelectedScanner(scanner);
                setIsConfirmDialogOpen(true);
            },
        },
    });

    const handleDeleteConfirm = () => {
        if (selectedScanner) {
            deleteScanner(selectedScanner.id, {
                onSuccess: () => setIsConfirmDialogOpen(false),
            });
        }
    };

    return (
        <>
            <ScannerDialog
                open={isFormDialogOpen}
                onOpenChange={setIsFormDialogOpen}
            />
            <ConfirmDialog
                open={isConfirmDialogOpen}
                onOpenChange={setIsConfirmDialogOpen}
                onConfirm={handleDeleteConfirm}
                title="Are you sure?"
                description={`This will permanently remove the scanner account for ${selectedScanner?.email}.`}
                isLoading={isDeletingScanner}
            />
            <div className="flex w-full flex-col justify-start gap-4">
                <div className="flex items-center justify-between">
                    <Input
                        placeholder="Filter scanners by email..."
                        value={(table.getColumn("email")?.getFilterValue() as string) ?? ""}
                        onChange={(event) => table.getColumn("email")?.setFilterValue(event.target.value)}
                        className="h-9 max-w-sm"
                    />
                    <Button size="sm" className="h-9" onClick={() => setIsFormDialogOpen(true)}>
                        <IconPlus className="mr-2 h-4 w-4"/>
                        <span>Add Scanner</span>
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
                                        Loading scanners...
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
                                        No scanners found.
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
