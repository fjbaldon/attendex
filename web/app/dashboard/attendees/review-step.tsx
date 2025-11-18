"use client";

import {AttendeeImportAnalysis, AttendeeRequest, InvalidRow} from "@/types";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {Button} from "@/components/ui/button";
import {useAttendees} from "@/hooks/use-attendees";
import {ColumnDef, flexRender, getCoreRowModel, getPaginationRowModel, useReactTable} from "@tanstack/react-table";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {DataTablePagination} from "@/components/shared/data-table-pagination";
import {IconDownload} from "@tabler/icons-react";

interface ReviewStepProps {
    analysisResult: AttendeeImportAnalysis;
    onCommitSuccess: (count: number) => void;
    onStartOver: () => void;
}

const validColumns: ColumnDef<AttendeeRequest>[] = [
    {accessorKey: "identity", header: "Identifier"},
    {accessorKey: "lastName", header: "Last Name"},
    {accessorKey: "firstName", header: "First Name"},
];

const invalidColumns: ColumnDef<InvalidRow>[] = [
    {accessorKey: "rowNumber", header: "Row #"},
    {
        accessorKey: "error",
        header: "Error",
        cell: ({row}) => <span className="text-destructive">{row.original.error}</span>
    },
];

export function ReviewStep({analysisResult, onCommitSuccess, onStartOver}: ReviewStepProps) {
    const {validAttendees, invalidRows} = analysisResult;
    const {commitAttendees, isCommittingAttendees} = useAttendees();

    const validTable = useReactTable({
        data: validAttendees,
        columns: validColumns,
        getCoreRowModel: getCoreRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
    });

    const invalidTable = useReactTable({
        data: invalidRows,
        columns: invalidColumns,
        getCoreRowModel: getCoreRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
    });

    const handleImport = async () => {
        try {
            await commitAttendees({attendees: validAttendees});
            onCommitSuccess(validAttendees.length);
        } catch {
            // Error toast is handled by the hook
        }
    };

    const handleDownloadErrors = () => {
        if (invalidRows.length === 0) return;

        const headers = Object.keys(invalidRows[0].rowData);
        const csvRows = [
            [...headers, "Error"].join(','),
            ...invalidRows.map(row => {
                const values = headers.map(header => `"${(row.rowData[header] || '').replace(/"/g, '""')}"`);
                const error = `"${row.error.replace(/"/g, '""')}"`;
                return [...values, error].join(',');
            })
        ];

        const csvContent = csvRows.join('\n');
        const blob = new Blob([csvContent], {type: 'text/csv;charset=utf-8;'});
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', 'attendee_import_errors.csv');
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    };

    return (
        <div className="space-y-4">
            <div className="rounded-lg border bg-muted/50 p-4 text-center">
                <p className="font-semibold">
                    Analysis Complete: <span className="text-green-600">{validAttendees.length} valid rows</span> and
                    <span className="text-destructive"> {invalidRows.length} errors</span> found.
                </p>
            </div>

            <Tabs defaultValue="valid">
                <TabsList className="grid w-full grid-cols-2">
                    <TabsTrigger value="valid">Ready to Import ({validAttendees.length})</TabsTrigger>
                    <TabsTrigger value="errors">Errors to Fix ({invalidRows.length})</TabsTrigger>
                </TabsList>
                <TabsContent value="valid" className="mt-4 space-y-4">
                    <div className="rounded-md border">
                        <Table>
                            <TableHeader><TableRow><TableHead>Identifier</TableHead><TableHead>Last
                                Name</TableHead><TableHead>First Name</TableHead></TableRow></TableHeader>
                            <TableBody>
                                {validTable.getRowModel().rows.map(row => (
                                    <TableRow key={row.id}>
                                        {row.getVisibleCells().map(cell => (
                                            <TableCell
                                                key={cell.id}>{flexRender(cell.column.columnDef.cell, cell.getContext())}</TableCell>
                                        ))}
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </div>
                    <DataTablePagination table={validTable}/>
                </TabsContent>
                <TabsContent value="errors" className="mt-4 space-y-4">
                    <div className="rounded-md border">
                        <Table>
                            <TableHeader><TableRow><TableHead>Row
                                #</TableHead><TableHead>Error</TableHead></TableRow></TableHeader>
                            <TableBody>
                                {invalidTable.getRowModel().rows.map(row => (
                                    <TableRow key={row.id}>
                                        {row.getVisibleCells().map(cell => (
                                            <TableCell
                                                key={cell.id}>{flexRender(cell.column.columnDef.cell, cell.getContext())}</TableCell>
                                        ))}
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </div>
                    <DataTablePagination table={invalidTable}/>
                </TabsContent>
            </Tabs>
            <div className="flex justify-between items-center pt-4">
                <Button variant="outline" onClick={onStartOver} disabled={isCommittingAttendees}>Start Over</Button>
                <div className="flex items-center gap-2">
                    {invalidRows.length > 0 && (
                        <Button variant="secondary" onClick={handleDownloadErrors}>
                            <IconDownload className="mr-2 h-4 w-4"/>
                            Download Error Report
                        </Button>
                    )}
                    <Button onClick={handleImport} disabled={validAttendees.length === 0 || isCommittingAttendees}>
                        {isCommittingAttendees ? "Importing..." : `Import ${validAttendees.length} Valid Attendees`}
                    </Button>
                </div>
            </div>
        </div>
    );
}
