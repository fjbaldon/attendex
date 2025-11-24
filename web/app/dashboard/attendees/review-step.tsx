"use client";

import React, {useMemo} from "react"; // Added useMemo
import {AttendeeImportAnalysis, AttendeeRequest, InvalidRow} from "@/types";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {Button} from "@/components/ui/button";
import {useAttendees} from "@/hooks/use-attendees";
import {ColumnDef, flexRender, getCoreRowModel, getPaginationRowModel, useReactTable} from "@tanstack/react-table";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {DataTablePagination} from "@/components/shared/data-table-pagination";
import {IconAlertTriangle, IconCheck, IconDownload, IconRefresh} from "@tabler/icons-react";
import {Badge} from "@/components/ui/badge";

interface ReviewStepProps {
    analysisResult: AttendeeImportAnalysis;
    onCommitSuccess: (count: number) => void;
    onStartOver: () => void;
}

// ... keep columns definitions (validColumns, invalidColumns) as is ...
const validColumns: ColumnDef<AttendeeRequest & { isUpdate: boolean }>[] = [
    {
        accessorKey: "status",
        header: "Action",
        cell: ({row}) => row.original.isUpdate ? (
            <Badge variant="secondary" className="text-blue-600 bg-blue-50 hover:bg-blue-100">Update</Badge>
        ) : (
            <Badge variant="outline" className="text-green-600 bg-green-50 hover:bg-green-100">New</Badge>
        )
    },
    {accessorKey: "identity", header: "Identity"},
    {accessorKey: "lastName", header: "Last Name"},
    {accessorKey: "firstName", header: "First Name"},
];

const invalidColumns: ColumnDef<InvalidRow>[] = [
    {accessorKey: "rowNumber", header: "Row #"},
    {
        accessorKey: "error",
        header: "Error",
        cell: ({row}) => <span className="text-destructive font-medium">{row.original.error}</span>
    },
];

export function ReviewStep({analysisResult, onCommitSuccess, onStartOver}: ReviewStepProps) {
    const {attendeesToCreate, attendeesToUpdate, invalidRows, newAttributesToCreate} = analysisResult;
    const {commitAttendees, isCommittingAttendees} = useAttendees();

    // FIXED: Memoize this heavy operation.
    // Without this, every time the dialog tries to close (triggering a re-render),
    // this array runs .map() spread (...) logic, freezing the thread if N > 1000.
    const combinedList = useMemo(() => [
        ...attendeesToCreate.map(a => ({...a, isUpdate: false})),
        ...attendeesToUpdate.map(a => ({...a, isUpdate: true}))
    ], [attendeesToCreate, attendeesToUpdate]);

    const totalValid = combinedList.length;

    const validTable = useReactTable({
        data: combinedList,
        columns: validColumns,
        getCoreRowModel: getCoreRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        initialState: {
            pagination: {
                pageSize: 10, // Reduced from 50 to 10 to keep DOM light during transitions
            },
        },
    });

    const invalidTable = useReactTable({
        data: invalidRows,
        columns: invalidColumns,
        getCoreRowModel: getCoreRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        initialState: {
            pagination: {
                pageSize: 10,
            },
        },
    });

    // ... keep handleImport and handleDownloadErrors logic as is ...
    const handleImport = async () => {
        try {
            await commitAttendees({
                attendees: [...attendeesToCreate, ...attendeesToUpdate],
                updateExisting: attendeesToUpdate.length > 0,
                newAttributes: newAttributesToCreate
            });
            onCommitSuccess(totalValid);
        } catch {
            // Error toast handled by hook
        }
    };

    const handleDownloadErrors = () => {
        if (invalidRows.length === 0) return;

        const dataHeaders = Object.keys(invalidRows[0].rowData);
        const headers = ["Row Number", ...dataHeaders, "Error Message"];

        const csvRows = [
            headers.join(','),
            ...invalidRows.map(row => {
                const rowDataValues = dataHeaders.map(header =>
                    `"${(row.rowData[header] || '').replace(/"/g, '""')}"`
                );
                const error = `"${row.error.replace(/"/g, '""')}"`;
                return [row.rowNumber, ...rowDataValues, error].join(',');
            })
        ];

        const csvContent = csvRows.join('\n');
        const blob = new Blob([csvContent], {type: 'text/csv;charset=utf-8;'});
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', 'import_errors.csv');
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    };

    return (
        <div className="space-y-4 h-full flex flex-col">
            {/* ... keep UI structure exactly the same ... */}
            {/* Compact Stats Row */}
            <div className="grid grid-cols-3 gap-3 shrink-0">
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between rounded-md border bg-green-50/50 p-3">
                    <div className="flex items-center gap-2 mb-1 sm:mb-0">
                        <IconCheck className="h-4 w-4 text-green-600"/>
                        <span className="text-xs font-medium text-green-700 uppercase">New</span>
                    </div>
                    <span className="text-xl font-bold text-green-700">{attendeesToCreate.length}</span>
                </div>
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between rounded-md border bg-blue-50/50 p-3">
                    <div className="flex items-center gap-2 mb-1 sm:mb-0">
                        <IconRefresh className="h-4 w-4 text-blue-600"/>
                        <span className="text-xs font-medium text-blue-700 uppercase">Update</span>
                    </div>
                    <span className="text-xl font-bold text-blue-700">{attendeesToUpdate.length}</span>
                </div>
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between rounded-md border bg-red-50/50 p-3">
                    <div className="flex items-center gap-2 mb-1 sm:mb-0">
                        <IconAlertTriangle className="h-4 w-4 text-red-600"/>
                        <span className="text-xs font-medium text-red-700 uppercase">Error</span>
                    </div>
                    <span className="text-xl font-bold text-red-700">{invalidRows.length}</span>
                </div>
            </div>

            {newAttributesToCreate.length > 0 && (
                <div className="shrink-0 rounded-md bg-amber-50 p-2 px-3 border border-amber-200 text-xs text-amber-800 flex items-start gap-2">
                    <IconAlertTriangle className="h-4 w-4 shrink-0 mt-0.5" />
                    <div>
                        <span className="font-semibold">Notice:</span> New attributes will be created:
                        <span className="font-mono ml-1">{newAttributesToCreate.join(", ")}</span>
                    </div>
                </div>
            )}

            <Tabs defaultValue="valid" className="flex-1 flex flex-col min-h-0">
                <TabsList className="grid w-full grid-cols-2 shrink-0">
                    <TabsTrigger value="valid">Ready to Import ({totalValid})</TabsTrigger>
                    <TabsTrigger value="errors">Errors ({invalidRows.length})</TabsTrigger>
                </TabsList>

                {/* Valid Tab Content */}
                <TabsContent value="valid" className="mt-2 flex-1 flex flex-col gap-2 min-h-0 data-[state=inactive]:hidden">
                    <div className="rounded-md border flex-1 relative overflow-hidden">
                        <div className="absolute inset-0 overflow-auto">
                            <Table>
                                <TableHeader className="sticky top-0 bg-background z-10 shadow-sm">
                                    {validTable.getHeaderGroups().map(headerGroup => (
                                        <TableRow key={headerGroup.id}>
                                            {headerGroup.headers.map(header => (
                                                <TableHead key={header.id}>
                                                    {flexRender(header.column.columnDef.header, header.getContext())}
                                                </TableHead>
                                            ))}
                                        </TableRow>
                                    ))}
                                </TableHeader>
                                <TableBody>
                                    {validTable.getRowModel().rows?.length ? (
                                        validTable.getRowModel().rows.map(row => (
                                            <TableRow key={row.id}>
                                                {row.getVisibleCells().map(cell => (
                                                    <TableCell key={cell.id}>
                                                        {flexRender(cell.column.columnDef.cell, cell.getContext())}
                                                    </TableCell>
                                                ))}
                                            </TableRow>
                                        ))
                                    ) : (
                                        <TableRow>
                                            <TableCell colSpan={validColumns.length} className="h-24 text-center">
                                                No valid records found.
                                            </TableCell>
                                        </TableRow>
                                    )}
                                </TableBody>
                            </Table>
                        </div>
                    </div>
                    <div className="shrink-0">
                        <DataTablePagination table={validTable}/>
                    </div>
                </TabsContent>

                {/* Errors Tab Content */}
                <TabsContent value="errors" className="mt-2 flex-1 flex flex-col gap-2 min-h-0 data-[state=inactive]:hidden">
                    <div className="rounded-md border flex-1 relative overflow-hidden">
                        <div className="absolute inset-0 overflow-auto">
                            <Table>
                                <TableHeader className="sticky top-0 bg-background z-10 shadow-sm">
                                    {invalidTable.getHeaderGroups().map(headerGroup => (
                                        <TableRow key={headerGroup.id}>
                                            {headerGroup.headers.map(header => (
                                                <TableHead key={header.id}>
                                                    {flexRender(header.column.columnDef.header, header.getContext())}
                                                </TableHead>
                                            ))}
                                        </TableRow>
                                    ))}
                                </TableHeader>
                                <TableBody>
                                    {invalidTable.getRowModel().rows?.length ? (
                                        invalidTable.getRowModel().rows.map(row => (
                                            <TableRow key={row.id}>
                                                {row.getVisibleCells().map(cell => (
                                                    <TableCell key={cell.id}>
                                                        {flexRender(cell.column.columnDef.cell, cell.getContext())}
                                                    </TableCell>
                                                ))}
                                            </TableRow>
                                        ))
                                    ) : (
                                        <TableRow>
                                            <TableCell colSpan={invalidColumns.length} className="h-24 text-center">
                                                No errors found.
                                            </TableCell>
                                        </TableRow>
                                    )}
                                </TableBody>
                            </Table>
                        </div>
                    </div>
                    <div className="shrink-0">
                        <DataTablePagination table={invalidTable}/>
                    </div>
                </TabsContent>
            </Tabs>

            <div className="flex justify-between items-center pt-2 mt-auto border-t shrink-0">
                <Button variant="outline" onClick={onStartOver} disabled={isCommittingAttendees}>
                    Start Over
                </Button>
                <div className="flex items-center gap-2">
                    {invalidRows.length > 0 && (
                        <Button variant="secondary" onClick={handleDownloadErrors}>
                            <IconDownload className="mr-2 h-4 w-4"/>
                            Download Errors
                        </Button>
                    )}
                    <Button onClick={handleImport} disabled={totalValid === 0 || isCommittingAttendees}>
                        {isCommittingAttendees ? "Importing..." : `Import ${totalValid} Records`}
                    </Button>
                </div>
            </div>
        </div>
    );
}
