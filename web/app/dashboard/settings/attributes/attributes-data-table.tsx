"use client";

import * as React from "react";
import {ColumnDef} from "@tanstack/react-table";
import {IconPlus} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";
import {AttributeDialog} from "./attribute-dialog";
import {ConfirmDialog} from "@/components/shared/confirm-dialog";
import {useAttributes} from "@/hooks/use-attributes";
import {Attribute} from "@/types";
import {DataTable} from "@/components/shared/data-table";

interface AttributesDataTableProps {
    columns: ColumnDef<Attribute>[];
    data: Attribute[];
    isLoading: boolean;
}

export function AttributesDataTable({columns, data, isLoading}: AttributesDataTableProps) {
    const {deleteDefinition, isDeleting} = useAttributes();
    const [isDialogOpen, setIsDialogOpen] = React.useState(false);
    const [isConfirmOpen, setIsConfirmOpen] = React.useState(false);
    const [selectedAttribute, setSelectedAttribute] = React.useState<Attribute | null>(null);

    const handleDeleteConfirm = () => {
        if (selectedAttribute) {
            deleteDefinition(selectedAttribute.id, {onSuccess: () => setIsConfirmOpen(false)});
        }
    }

    const toolbar = (
        <div className="flex justify-start">
            <Button size="sm" className="h-9" onClick={() => {
                setSelectedAttribute(null);
                setIsDialogOpen(true);
            }}>
                <IconPlus className="mr-2 h-4 w-4"/>
                Add New Attribute
            </Button>
        </div>
    );

    return (
        <>
            <AttributeDialog
                key={selectedAttribute?.id || 'new'}
                open={isDialogOpen}
                onOpenChange={setIsDialogOpen}
                attribute={selectedAttribute}
            />
            <ConfirmDialog
                open={isConfirmOpen}
                onOpenChange={setIsConfirmOpen}
                onConfirm={handleDeleteConfirm}
                title="Are you sure?"
                description={`This will permanently delete the "${selectedAttribute?.name}" attribute. This action cannot be undone.`}
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
                    openEditDialog: (attribute: Attribute) => {
                        setSelectedAttribute(attribute);
                        setIsDialogOpen(true);
                    },
                    openDeleteDialog: (attribute: Attribute) => {
                        setSelectedAttribute(attribute);
                        setIsConfirmOpen(true);
                    },
                }}
            />
        </>
    );
}