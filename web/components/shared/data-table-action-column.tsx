"use client";

import {ColumnDef, Row, Table} from "@tanstack/react-table";
import {Button} from "@/components/ui/button";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip";
import React from "react";
import {cn} from "@/lib/utils";

export interface DataTableAction<TData> {
    icon: React.ElementType;
    label: string;
    onClick: (row: Row<TData>, table: Table<TData>) => void;
    isDestructive?: boolean;
}

export const createActionsColumn = <TData extends object>(
    actions: DataTableAction<TData>[]
): ColumnDef<TData> => {
    return {
        id: "actions",
        cell: ({row, table}) => {
            return (
                <div className="flex items-center justify-end gap-2">
                    {actions.map((action, index) => (
                        <Tooltip key={index}>
                            <TooltipTrigger asChild>
                                <Button
                                    variant="ghost"
                                    size="icon"
                                    className={cn(
                                        "h-8 w-8",
                                        action.isDestructive && "text-destructive hover:text-destructive"
                                    )}
                                    onClick={() => action.onClick(row, table)}
                                >
                                    <action.icon className="h-4 w-4"/>
                                    <span className="sr-only">{action.label}</span>
                                </Button>
                            </TooltipTrigger>
                            <TooltipContent>{action.label}</TooltipContent>
                        </Tooltip>
                    ))}
                </div>
            );
        },
    };
};
