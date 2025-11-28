"use client";

import * as React from "react";
import {
    ColumnDef,
    ColumnFiltersState,
    flexRender,
    getCoreRowModel,
    getFilteredRowModel,
    getPaginationRowModel,
    getSortedRowModel,
    OnChangeFn,
    RowSelectionState,
    SortingState,
    TableMeta,
    useReactTable,
    VisibilityState,
} from "@tanstack/react-table";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {DataTablePagination} from "@/components/shared/data-table-pagination";
import {
    DropdownMenu,
    DropdownMenuCheckboxItem,
    DropdownMenuContent,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger
} from "@/components/ui/dropdown-menu";
import {Button} from "@/components/ui/button";
import {IconChevronDown, IconColumns} from "@tabler/icons-react";
import {Card} from "@/components/ui/card";
import {Collapsible, CollapsibleContent, CollapsibleTrigger} from "@/components/ui/collapsible";

interface DataTableProps<TData> {
    columns: ColumnDef<TData>[];
    data: TData[];
    isLoading: boolean;
    pageCount: number;
    pagination: { pageIndex: number; pageSize: number; };
    setPagination: (pagination: { pageIndex: number; pageSize: number; }) => void;
    toolbar: React.ReactNode;
    meta?: TableMeta<TData>;
    state?: { rowSelection: RowSelectionState };
    onRowSelectionChange?: OnChangeFn<RowSelectionState>;
}

export function DataTable<TData>({
                                     columns,
                                     data,
                                     isLoading,
                                     pageCount,
                                     pagination,
                                     setPagination,
                                     toolbar,
                                     meta,
                                     state: controlledState,
                                     onRowSelectionChange,
                                 }: DataTableProps<TData>) {
    const [sorting, setSorting] = React.useState<SortingState>([]);
    const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([]);
    const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({});
    const [internalRowSelection, setInternalRowSelection] = React.useState<RowSelectionState>({});

    const table = useReactTable({
        data,
        columns,
        pageCount,
        state: {
            sorting,
            columnFilters,
            columnVisibility,
            rowSelection: controlledState?.rowSelection ?? internalRowSelection,
            pagination
        },
        onSortingChange: setSorting,
        onColumnFiltersChange: setColumnFilters,
        onColumnVisibilityChange: setColumnVisibility,
        onRowSelectionChange: onRowSelectionChange ?? setInternalRowSelection,
        getCoreRowModel: getCoreRowModel(),
        getSortedRowModel: getSortedRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        onPaginationChange: (updater) => {
            if (typeof updater === 'function') {
                setPagination(updater(pagination));
            } else {
                setPagination(updater);
            }
        },
        manualPagination: true,
        meta,
    });

    return (
        <div className="flex w-full flex-col justify-start gap-4">
            <div className="flex items-center justify-between gap-2">
                <div className="flex-1">
                    {toolbar}
                </div>

                <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                        <Button variant="outline" className="ml-auto h-9 hidden sm:flex">
                            <IconColumns className="mr-2 h-4 w-4" />
                            View
                        </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end" className="w-[200px] max-h-[300px] overflow-y-auto">
                        <DropdownMenuLabel>Toggle Columns</DropdownMenuLabel>
                        <DropdownMenuSeparator />
                        {table
                            .getAllColumns()
                            .filter((column) => typeof column.accessorFn !== "undefined" && column.getCanHide())
                            .map((column) => {
                                return (
                                    <DropdownMenuCheckboxItem
                                        key={column.id}
                                        className="capitalize"
                                        checked={column.getIsVisible()}
                                        onCheckedChange={(value) => column.toggleVisibility(value)}
                                    >
                                        {typeof column.columnDef.header === 'string'
                                            ? column.columnDef.header
                                            : column.id.replace(/_/g, ' ').replace(/attributes\./g, '')}
                                    </DropdownMenuCheckboxItem>
                                )
                            })}
                    </DropdownMenuContent>
                </DropdownMenu>
            </div>

            {/* DESKTOP VIEW */}
            <div className="hidden md:block rounded-lg border overflow-hidden">
                <div className="overflow-x-auto">
                    <Table>
                        <TableHeader className="bg-muted">
                            {table.getHeaderGroups().map((headerGroup) => (
                                <TableRow key={headerGroup.id}>
                                    {headerGroup.headers.map((header) => (
                                        <TableHead key={header.id} className="whitespace-nowrap">
                                            {header.isPlaceholder
                                                ? null
                                                : flexRender(header.column.columnDef.header, header.getContext())}
                                        </TableHead>
                                    ))}
                                </TableRow>
                            ))}
                        </TableHeader>
                        <TableBody>
                            {isLoading ? (
                                <TableRow>
                                    <TableCell colSpan={columns.length} className="h-24 text-center">
                                        Loading data...
                                    </TableCell>
                                </TableRow>
                            ) : table.getRowModel().rows?.length ? (
                                table.getRowModel().rows.map((row) => (
                                    <TableRow key={row.id} data-state={row.getIsSelected() && "selected"}>
                                        {row.getVisibleCells().map((cell) => (
                                            <TableCell key={cell.id} className="whitespace-nowrap">
                                                {flexRender(cell.column.columnDef.cell, cell.getContext())}
                                            </TableCell>
                                        ))}
                                    </TableRow>
                                ))
                            ) : (
                                <TableRow>
                                    <TableCell colSpan={columns.length} className="h-24 text-center">
                                        No results found.
                                    </TableCell>
                                </TableRow>
                            )}
                        </TableBody>
                    </Table>
                </div>
            </div>

            {/* MOBILE CARD VIEW */}
            <div className="md:hidden space-y-4">
                {isLoading ? (
                    <div className="text-center py-10 text-muted-foreground">Loading...</div>
                ) : table.getRowModel().rows.length > 0 ? (
                    table.getRowModel().rows.map((row) => {
                        // FIX: Store select cell in a variable to avoid ! assertion error
                        const selectCell = row.getVisibleCells().find(c => c.column.id === 'select');

                        return (
                            <Card key={row.id} className="p-4 flex flex-col gap-3">
                                {/* Render first 3 columns as header info */}
                                <div className="flex justify-between items-start">
                                    <div className="flex flex-col gap-1">
                                        {row.getVisibleCells().slice(0, 2).map(cell => (
                                            <div key={cell.id} className="text-sm">
                                                {flexRender(cell.column.columnDef.cell, cell.getContext())}
                                            </div>
                                        ))}
                                    </div>
                                    {/* Selection Checkbox if present */}
                                    {selectCell && (
                                        <div>
                                            {flexRender(selectCell.column.columnDef.cell, selectCell.getContext())}
                                        </div>
                                    )}
                                </div>

                                <Collapsible>
                                    <CollapsibleTrigger asChild>
                                        <Button variant="ghost" size="sm" className="w-full justify-between">
                                            View Details <IconChevronDown className="h-4 w-4" />
                                        </Button>
                                    </CollapsibleTrigger>
                                    <CollapsibleContent className="space-y-2 mt-2 pt-2 border-t text-sm">
                                        {row.getVisibleCells().slice(2).map(cell => {
                                            if (cell.column.id === 'select') return null;
                                            return (
                                                <div key={cell.id} className="flex justify-between py-1">
                                                    <span className="font-medium text-muted-foreground">
                                                        {typeof cell.column.columnDef.header === 'string' ? cell.column.columnDef.header : cell.column.id}
                                                    </span>
                                                    <span>{flexRender(cell.column.columnDef.cell, cell.getContext())}</span>
                                                </div>
                                            );
                                        })}
                                    </CollapsibleContent>
                                </Collapsible>
                            </Card>
                        );
                    })
                ) : (
                    <div className="text-center py-10 text-muted-foreground border rounded-lg">No results found.</div>
                )}
            </div>

            <div className="pt-2">
                <DataTablePagination table={table}/>
            </div>
        </div>
    );
}
