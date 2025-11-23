"use client";

import {ColumnDef} from "@tanstack/react-table";
import {ScannerResponse} from "@/types";
import {IconKey, IconLock, IconLockOpen, IconTrash} from "@tabler/icons-react";
import {Badge} from "@/components/ui/badge";
import {Button} from "@/components/ui/button";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip";

interface ScannersTableMeta {
    openDeleteDialog: (scanner: ScannerResponse) => void;
    openResetPasswordDialog: (scanner: ScannerResponse) => void;
}

export const getColumns = (onToggleStatus: (id: number) => void): ColumnDef<ScannerResponse>[] => [
    {
        accessorKey: "email",
        header: () => <div className="pl-4">Email Address</div>,
        cell: ({ row }) => {
            return <div className="pl-4 font-medium">{row.original.email}</div>;
        }
    },
    {
        accessorKey: "enabled",
        header: "Status",
        cell: ({ row }) => {
            const isEnabled = row.original.enabled;
            return (
                <Badge
                    variant={isEnabled ? "outline" : "secondary"}
                    className={isEnabled
                        ? "bg-green-50 text-green-700 border-green-200 hover:bg-green-100"
                        : "bg-gray-100 text-gray-600 hover:bg-gray-200"}
                >
                    {isEnabled ? "Active" : "Suspended"}
                </Badge>
            );
        }
    },
    {
        id: "actions",
        cell: ({ row, table }) => {
            const scanner = row.original;
            const meta = table.options.meta as ScannersTableMeta | undefined;

            return (
                <div className="flex items-center justify-end gap-2">
                    {/* Action 1: Toggle Status */}
                    <Tooltip>
                        <TooltipTrigger asChild>
                            <Button
                                variant="ghost"
                                size="icon"
                                className="h-8 w-8"
                                onClick={() => onToggleStatus(scanner.id)}
                            >
                                {scanner.enabled ? (
                                    <IconLock className="h-4 w-4" />
                                ) : (
                                    <IconLockOpen className="h-4 w-4" />
                                )}
                                <span className="sr-only">
                                    {scanner.enabled ? "Suspend Access" : "Activate Access"}
                                </span>
                            </Button>
                        </TooltipTrigger>
                        <TooltipContent>
                            {scanner.enabled ? "Suspend Access" : "Activate Access"}
                        </TooltipContent>
                    </Tooltip>

                    {/* Action 2: Reset Password */}
                    <Tooltip>
                        <TooltipTrigger asChild>
                            <Button
                                variant="ghost"
                                size="icon"
                                className="h-8 w-8"
                                onClick={() => meta?.openResetPasswordDialog(scanner)}
                            >
                                <IconKey className="h-4 w-4" />
                                <span className="sr-only">Reset Password</span>
                            </Button>
                        </TooltipTrigger>
                        <TooltipContent>Reset Password</TooltipContent>
                    </Tooltip>

                    {/* Action 3: Remove Scanner */}
                    <Tooltip>
                        <TooltipTrigger asChild>
                            <Button
                                variant="ghost"
                                size="icon"
                                className="h-8 w-8 text-destructive hover:text-destructive"
                                onClick={() => meta?.openDeleteDialog(scanner)}
                            >
                                <IconTrash className="h-4 w-4" />
                                <span className="sr-only">Remove Scanner</span>
                            </Button>
                        </TooltipTrigger>
                        <TooltipContent>Remove Scanner</TooltipContent>
                    </Tooltip>
                </div>
            );
        },
    },
];
