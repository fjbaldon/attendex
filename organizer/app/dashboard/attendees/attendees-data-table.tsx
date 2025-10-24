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
import {IconBulb, IconHelpCircle, IconPlus, IconUpload} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {AttendeeResponse} from "@/types";
import {useAttendees} from "@/hooks/use-attendees";
import {ConfirmDialog} from "@/components/shared/confirm-dialog";
import {DataTablePagination} from "@/components/shared/data-table-pagination";
import {Input} from "@/components/ui/input";
import {AttendeeDialog} from "./attendee-dialog";
import {useRef, useState} from "react";
import {toast} from "sonner";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
    DialogTrigger
} from "@/components/ui/dialog";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {Badge} from "@/components/ui/badge";
import api from "@/lib/api";

interface AttendeesDataTableProps {
    columns: ColumnDef<AttendeeResponse>[];
    data: AttendeeResponse[];
    isLoading: boolean;
    pageCount: number;
    pagination: { pageIndex: number; pageSize: number; };
    setPagination: (pagination: { pageIndex: number; pageSize: number; }) => void;
}

const fieldTypeRules = [
    {type: "TEXT", rule: "Accepts any sequence of characters.", example: "Computer Science"},
    {type: "NUMBER", rule: "Must be a valid number, including decimals.", example: "123.45"},
    {type: "DATE", rule: "Must be in YYYY-MM-DD format.", example: "2025-10-28"},
    {type: "SELECT", rule: "Must be an exact match to one of the predefined options.", example: "Option A"},
];

export function AttendeesDataTable({
                                       columns,
                                       data,
                                       isLoading,
                                       pageCount,
                                       pagination,
                                       setPagination
                                   }: AttendeesDataTableProps) {
    const {deleteAttendee, isDeletingAttendee, importAttendees, isImportingAttendees} = useAttendees();
    const fileInputRef = useRef<HTMLInputElement>(null);
    const [isDownloadingTemplate, setIsDownloadingTemplate] = useState(false);

    const [sorting, setSorting] = React.useState<SortingState>([]);
    const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([]);
    const [rowSelection, setRowSelection] = React.useState({});
    const [isFormDialogOpen, setIsFormDialogOpen] = React.useState(false);
    const [isConfirmDialogOpen, setIsConfirmDialogOpen] = React.useState(false);
    const [selectedAttendee, setSelectedAttendee] = React.useState<AttendeeResponse | null>(null);

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
            if (typeof updater === 'function') {
                setPagination(updater(pagination));
            } else {
                setPagination(updater);
            }
        },
        manualPagination: true,
        meta: {
            openEditDialog: (attendee: AttendeeResponse) => {
                setSelectedAttendee(attendee);
                setIsFormDialogOpen(true);
            },
            openDeleteDialog: (attendee: AttendeeResponse) => {
                setSelectedAttendee(attendee);
                setIsConfirmDialogOpen(true);
            },
        },
    });

    const handleOpenCreateDialog = () => {
        setSelectedAttendee(null);
        setIsFormDialogOpen(true);
    };

    const handleDeleteConfirm = () => {
        if (selectedAttendee) {
            deleteAttendee(selectedAttendee.id, {
                onSuccess: () => setIsConfirmDialogOpen(false),
            });
        }
    };

    const handleImportClick = () => {
        fileInputRef.current?.click();
    };

    const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        if (file) {
            if (file.type !== "text/csv") {
                toast.error("Invalid File Type", {
                    description: "Please upload a valid .csv file.",
                });
                return;
            }
            importAttendees(file);
        }
        if (event.target) {
            event.target.value = "";
        }
    };

    const handleDownloadTemplate = async () => {
        setIsDownloadingTemplate(true);
        try {
            const response = await api.get('/api/v1/attendees/import-template', {
                responseType: 'blob',
            });
            const blob = new Blob([response.data], {type: 'text/csv'});
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', 'attendee_template.csv');
            document.body.appendChild(link);
            link.click();
            link.parentNode?.removeChild(link);
            window.URL.revokeObjectURL(url);
        } catch {
            toast.error("Download Failed", {
                description: "Could not download the template file. Please try again."
            });
        } finally {
            setIsDownloadingTemplate(false);
        }
    };

    return (
        <>
            <AttendeeDialog
                open={isFormDialogOpen}
                onOpenChange={setIsFormDialogOpen}
                attendee={selectedAttendee}
            />
            <ConfirmDialog
                open={isConfirmDialogOpen}
                onOpenChange={setIsConfirmDialogOpen}
                onConfirm={handleDeleteConfirm}
                title="Are you sure?"
                description={`This will permanently delete ${selectedAttendee?.firstName} ${selectedAttendee?.lastName}. This action cannot be undone.`}
                isLoading={isDeletingAttendee}
            />
            <div className="flex w-full flex-col justify-start gap-4">
                <div className="flex items-center justify-between">
                    <Input
                        placeholder="Filter attendees by name..."
                        value={(table.getColumn("lastName")?.getFilterValue() as string) ?? ""}
                        onChange={(event) =>
                            table.getColumn("lastName")?.setFilterValue(event.target.value)
                        }
                        className="h-9 max-w-sm"
                    />
                    <div className="flex items-center gap-2">
                        <Dialog>
                            <DialogTrigger asChild>
                                <Button size="icon" variant="outline" className="h-9 w-9">
                                    <IconHelpCircle className="h-4 w-4"/>
                                    <span className="sr-only">Import Guidelines</span>
                                </Button>
                            </DialogTrigger>
                            <DialogContent className="sm:max-w-2xl">
                                <DialogHeader>
                                    <DialogTitle>CSV Import Guidelines</DialogTitle>
                                    <DialogDescription>
                                        Follow these guidelines to ensure your attendees are imported correctly.
                                    </DialogDescription>
                                </DialogHeader>

                                <Tabs defaultValue="required" className="pt-4">
                                    <TabsList className="grid w-full grid-cols-3">
                                        <TabsTrigger value="required">Required Fields</TabsTrigger>
                                        <TabsTrigger value="custom">Custom Fields</TabsTrigger>
                                        <TabsTrigger value="example">Full Example</TabsTrigger>
                                    </TabsList>
                                    <TabsContent value="required" className="pt-4">
                                        <p className="text-sm text-muted-foreground">Your CSV file must contain these
                                            columns. Headers are case-insensitive.</p>
                                        <div className="mt-4 space-y-3 rounded-lg border p-4">
                                            <div className="grid grid-cols-[180px_1fr] items-start gap-x-6">
                                                <code className="font-semibold">uniqueIdentifier</code>
                                                <p className="text-sm text-muted-foreground">A unique ID for each
                                                    person. Must not exist in the database or be repeated in the
                                                    file.</p>
                                            </div>
                                            <div className="grid grid-cols-[180px_1fr] items-start gap-x-6">
                                                <code className="font-semibold">firstName</code>
                                                <p className="text-sm text-muted-foreground">The first name of the
                                                    attendee.</p>
                                            </div>
                                            <div className="grid grid-cols-[180px_1fr] items-start gap-x-6">
                                                <code className="font-semibold">lastName</code>
                                                <p className="text-sm text-muted-foreground">The last name of the
                                                    attendee.</p>
                                            </div>
                                        </div>
                                    </TabsContent>
                                    <TabsContent value="custom" className="pt-4">
                                        <p className="text-sm text-muted-foreground">Additional columns matching your
                                            defined custom fields will be imported. The following format rules are
                                            enforced.</p>
                                        <div className="mt-4 space-y-4 rounded-lg border p-4">
                                            {fieldTypeRules.map(rule => (
                                                <div key={rule.type}
                                                     className="grid grid-cols-[80px_1fr_1fr] items-start gap-x-4">
                                                    <Badge variant="secondary" className="mt-1">{rule.type}</Badge>
                                                    <p className="text-sm">{rule.rule}</p>
                                                    <code className="text-sm text-muted-foreground">e.g.,
                                                        &#34;{rule.example}&#34;</code>
                                                </div>
                                            ))}
                                        </div>
                                    </TabsContent>
                                    <TabsContent value="example" className="pt-4">
                                        <p className="text-sm text-muted-foreground">This is an example of a valid CSV
                                            structure with required and custom fields.</p>
                                        <pre className="text-xs mt-4 rounded-md bg-muted p-4 overflow-x-auto">
                                            <code>
                                                uniqueIdentifier,firstName,lastName,year,block,program,enrollmentDate<br/>
                                                2024001,John,Smith,1,A,Computer Science,2024-08-26<br/>
                                                2024002,Jane,Doe,2,B,Information Technology,2023-08-28<br/>
                                                2024003,Michael,Johnson,3,A,Computer Engineering,2022-08-22
                                            </code>
                                        </pre>
                                    </TabsContent>
                                </Tabs>

                                <div className="mt-6 relative rounded-lg border bg-background p-4">
                                    <div className="flex items-start gap-4">
                                        <div className="text-foreground pt-0.5">
                                            <IconBulb className="h-5 w-5"/>
                                        </div>
                                        <div className="flex-1">
                                            <h5 className="font-medium text-foreground">Pro Tip</h5>
                                            <p className="text-sm text-muted-foreground mb-4">
                                                The easiest way to avoid errors is to download a template. It will
                                                automatically include all required and custom field headers for you.
                                            </p>
                                            <Button variant="secondary" className="w-full sm:w-auto"
                                                    onClick={handleDownloadTemplate} disabled={isDownloadingTemplate}>
                                                {isDownloadingTemplate ? "Downloading..." : "Download CSV Template"}
                                            </Button>
                                        </div>
                                    </div>
                                </div>
                            </DialogContent>
                        </Dialog>

                        <Input
                            type="file"
                            ref={fileInputRef}
                            onChange={handleFileChange}
                            className="hidden"
                            accept=".csv"
                        />
                        <Button size="sm" variant="outline" className="h-9" onClick={handleImportClick}
                                disabled={isImportingAttendees}>
                            <IconUpload className="mr-2 h-4 w-4"/>
                            {isImportingAttendees ? "Importing..." : "Import CSV"}
                        </Button>
                        <Button size="sm" className="h-9" onClick={handleOpenCreateDialog}>
                            <IconPlus className="mr-2 h-4 w-4"/>
                            <span>Add Attendee</span>
                        </Button>
                    </div>
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
                                        Loading attendees...
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
                                        No attendees found.
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
