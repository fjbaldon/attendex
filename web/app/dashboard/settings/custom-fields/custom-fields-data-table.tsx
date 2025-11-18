"use client";

import * as React from "react";
import {ColumnDef} from "@tanstack/react-table";
import {IconPlus} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";
import {CustomFieldDialog} from "./custom-field-dialog";
import {ConfirmDialog} from "@/components/shared/confirm-dialog";
import {useCustomFields} from "@/hooks/use-custom-fields";
import {CustomFieldDefinition} from "@/types";
import {DataTable} from "@/components/shared/data-table";

interface CustomFieldsDataTableProps {
    columns: ColumnDef<CustomFieldDefinition>[];
    data: CustomFieldDefinition[];
    isLoading: boolean;
}

export function CustomFieldsDataTable({columns, data, isLoading}: CustomFieldsDataTableProps) {
    const {deleteDefinition, isDeleting} = useCustomFields();
    const [isDialogOpen, setIsDialogOpen] = React.useState(false);
    const [isConfirmOpen, setIsConfirmOpen] = React.useState(false);
    const [selectedField, setSelectedField] = React.useState<CustomFieldDefinition | null>(null);

    const handleDeleteConfirm = () => {
        if (selectedField) {
            deleteDefinition(selectedField.id, {onSuccess: () => setIsConfirmOpen(false)});
        }
    }

    const toolbar = (
        <div className="flex justify-start">
            <Button size="sm" className="h-9" onClick={() => {
                setSelectedField(null);
                setIsDialogOpen(true);
            }}>
                <IconPlus className="mr-2 h-4 w-4"/>
                Add New Field
            </Button>
        </div>
    );

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
            <DataTable
                columns={columns}
                data={data}
                isLoading={isLoading}
                pageCount={1}
                pagination={{pageIndex: 0, pageSize: data.length}}
                setPagination={() => {
                }}
                toolbar={toolbar}
                meta={{
                    openEditDialog: (field: CustomFieldDefinition) => {
                        setSelectedField(field);
                        setIsDialogOpen(true);
                    },
                    openDeleteDialog: (field: CustomFieldDefinition) => {
                        setSelectedField(field);
                        setIsConfirmOpen(true);
                    },
                }}
            />
        </>
    );
}
