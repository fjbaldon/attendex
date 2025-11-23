"use client";

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

// Define columns for the valid data table
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

// Define columns for the invalid data table
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

    // Combine lists for display, flagging them so we can show a badge
    const combinedList = [
        ...attendeesToCreate.map(a => ({...a, isUpdate: false})),
        ...attendeesToUpdate.map(a => ({...a, isUpdate: true}))
    ];

    const totalValid = combinedList.length;

    const validTable = useReactTable({
        data: combinedList,
        columns: validColumns,
        getCoreRowModel: getCoreRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        initialState: {
            pagination: {
                pageSize: 5,
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
                pageSize: 5,
            },
        },
    });

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

        // Dynamically get headers from the first row's data map
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
        <div className="space-y-6 h-full flex flex-col">
            <div className="grid grid-cols-3 gap-4">
                <div className="rounded-lg border bg-green-50/50 p-4 text-center">
                    <div className="flex justify-center mb-2"><IconCheck className="text-green-600"/></div>
                    <div className="text-2xl font-bold text-green-700">{attendeesToCreate.length}</div>
                    <div className="text-xs text-green-600 font-medium uppercase">New Records</div>
                </div>
                <div className="rounded-lg border bg-blue-50/50 p-4 text-center">
                    <div className="flex justify-center mb-2"><IconRefresh className="text-blue-600"/></div>
                    <div className="text-2xl font-bold text-blue-700">{attendeesToUpdate.length}</div>
                    <div className="text-xs text-blue-600 font-medium uppercase">Updates</div>
                </div>
                <div className="rounded-lg border bg-red-50/50 p-4 text-center">
                    <div className="flex justify-center mb-2"><IconAlertTriangle className="text-red-600"/></div>
                    <div className="text-2xl font-bold text-red-700">{invalidRows.length}</div>
                    <div className="text-xs text-red-600 font-medium uppercase">Errors</div>
                </div>
            </div>

            {newAttributesToCreate.length > 0 && (
                <div className="rounded-md bg-amber-50 p-3 border border-amber-200 text-sm text-amber-800">
                    <span className="font-semibold">Note:</span> The following new attributes will be created:
                    <span className="font-mono ml-2">{newAttributesToCreate.join(", ")}</span>
                </div>
            )}

            <Tabs defaultValue="valid" className="flex-1 flex flex-col min-h-0">
                <TabsList className="grid w-full grid-cols-2">
                    <TabsTrigger value="valid">Ready to Import ({totalValid})</TabsTrigger>
                    <TabsTrigger value="errors">Errors ({invalidRows.length})</TabsTrigger>
                </TabsList>

                <TabsContent value="valid" className="mt-4 flex-1 flex flex-col gap-4 min-h-0">
                    <div className="rounded-md border overflow-auto flex-1">
                        <Table>
                            <TableHeader>
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
                    <DataTablePagination table={validTable}/>
                </TabsContent>

                <TabsContent value="errors" className="mt-4 flex-1 flex flex-col gap-4 min-h-0">
                    <div className="rounded-md border overflow-auto flex-1">
                        <Table>
                            <TableHeader>
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
                    <DataTablePagination table={invalidTable}/>
                </TabsContent>
            </Tabs>

            <div className="flex justify-between items-center pt-2 mt-auto border-t">
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
