"use client";

import * as React from "react";
import {
    ColumnDef,
    flexRender,
    getCoreRowModel,
    getSortedRowModel,
    useReactTable,
    SortingState
} from "@tanstack/react-table";
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {Button} from "@/components/ui/button";
import {IconPlus} from "@tabler/icons-react";
import {CustomFieldDialog} from "./custom-field-dialog";
import {ConfirmDialog} from "@/components/shared/confirm-dialog";
import {useCustomFields} from "@/hooks/use-custom-fields";
import {CustomFieldDefinition} from "@/types";
import {CardAction} from "@/components/ui/card";

interface CustomFieldsDataTableProps {
    columns: ColumnDef<CustomFieldDefinition>[];
    data: CustomFieldDefinition[];
    isLoading: boolean;
}

export function CustomFieldsDataTable({columns, data, isLoading}: CustomFieldsDataTableProps) {
    const {deleteDefinition, isDeleting} = useCustomFields();
    const [sorting, setSorting] = React.useState<SortingState>([]);
    const [isDialogOpen, setIsDialogOpen] = React.useState(false);
    const [isConfirmOpen, setIsConfirmOpen] = React.useState(false);
    const [selectedField, setSelectedField] = React.useState<CustomFieldDefinition | null>(null);

    const table = useReactTable({
        data,
        columns,
        state: {sorting},
        onSortingChange: setSorting,
        getCoreRowModel: getCoreRowModel(),
        getSortedRowModel: getSortedRowModel(),
        meta: {
            openEditDialog: (field: CustomFieldDefinition) => {
                setSelectedField(field);
                setIsDialogOpen(true);
            },
            openDeleteDialog: (field: CustomFieldDefinition) => {
                setSelectedField(field);
                setIsConfirmOpen(true);
            },
        },
    });

    const handleOpenCreateDialog = () => {
        setSelectedField(null);
        setIsDialogOpen(true);
    }

    const handleDeleteConfirm = () => {
        if (selectedField) {
            deleteDefinition(selectedField.id, {onSuccess: () => setIsConfirmOpen(false)});
        }
    }

    return (
        <>
            <CustomFieldDialog
                key={selectedField?.id || 'new'}
                open={isDialogOpen}
                onOpenChange={setIsDialogOpen}
                field={selectedField}
            />
            <ConfirmDialog
                open={isConfirmOpen}
                onOpenChange={setIsConfirmOpen}
                onConfirm={handleDeleteConfirm}
                title="Are you sure?"
                description={`This will permanently delete the "${selectedField?.fieldName}" field. This action cannot be undone.`}
                isLoading={isDeleting}
            />
            <Card>
                <CardHeader>
                    <CardTitle>Manage Custom Fields</CardTitle>
                    <CardDescription>
                        Add, edit, or remove custom fields for attendees in your organization.
                    </CardDescription>
                    <CardAction>
                        <Button size="sm" onClick={handleOpenCreateDialog}>
                            <IconPlus className="mr-2 h-4 w-4"/>
                            Add New Field
                        </Button>
                    </CardAction>
                </CardHeader>
                <CardContent>
                    <div className="rounded-lg border">
                        <Table>
                            <TableHeader>
                                {table.getHeaderGroups().map((headerGroup) => (
                                    <TableRow key={headerGroup.id}>
                                        {headerGroup.headers.map((header) => (
                                            <TableHead key={header.id}>
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
                                            Loading fields...
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
                                            No custom fields defined yet.
                                        </TableCell>
                                    </TableRow>
                                )}
                            </TableBody>
                        </Table>
                    </div>
                </CardContent>
            </Card>
        </>
    );
}
