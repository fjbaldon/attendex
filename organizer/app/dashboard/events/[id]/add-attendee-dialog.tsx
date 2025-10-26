"use client";

import * as React from "react";
import {useEffect, useMemo, useState} from "react";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle
} from "@/components/ui/dialog";
import {useAttendees} from "@/hooks/use-attendees";
import {useEventDetails} from "@/hooks/use-event-details";
import {AttendeeResponse} from "@/types";
import {Button} from "@/components/ui/button";
import {useDebounce} from "@uidotdev/usehooks";
import {
    useReactTable,
    getCoreRowModel,
    getPaginationRowModel,
    getFilteredRowModel,
    flexRender
} from "@tanstack/react-table";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {Input} from "@/components/ui/input";
import {DataTablePagination} from "@/components/shared/data-table-pagination";
import {getColumns} from "./add-attendees-columns";
import {useCustomFields} from "@/hooks/use-custom-fields";
import {Skeleton} from "@/components/ui/skeleton";
import {
    DropdownMenu,
    DropdownMenuCheckboxItem,
    DropdownMenuContent,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger
} from "@/components/ui/dropdown-menu";
import {IconChevronDown, IconFilterOff} from "@tabler/icons-react";
import {toast} from "sonner";

const FilterDropdown = ({field, attendees, activeFilters, onFilterChange}: {
    field: string;
    attendees: AttendeeResponse[];
    activeFilters: Record<string, string[]>;
    onFilterChange: (filters: Record<string, string[]>) => void;
}) => {
    const options = useMemo(() => Array.from(
        attendees.reduce((acc, attendee) => {
            const value = attendee.customFields?.[field];
            if (value != null && String(value).trim() !== "") {
                acc.add(String(value));
            }
            return acc;
        }, new Set<string>())
    ).sort(), [field, attendees]);

    const handleSelect = (value: string, isSelected: boolean) => {
        const currentSelection = activeFilters[field] || [];
        const newSelection = isSelected ? [...currentSelection, value] : currentSelection.filter(item => item !== value);
        onFilterChange({...activeFilters, [field]: newSelection});
    };

    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button variant="outline" size="sm" className="h-8">
                    {field}
                    {activeFilters[field]?.length > 0 && ` (${activeFilters[field].length})`}
                    <IconChevronDown className="ml-2 h-4 w-4" stroke={1.5}/>
                </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent className="w-56">
                <DropdownMenuLabel>{`Filter by ${field}`}</DropdownMenuLabel>
                <DropdownMenuSeparator/>
                {options.map(option => (
                    <DropdownMenuCheckboxItem key={option}
                                              checked={activeFilters[field]?.includes(option)}
                                              onCheckedChange={(isSelected) => handleSelect(option, isSelected)}>
                        {option}
                    </DropdownMenuCheckboxItem>
                ))}
            </DropdownMenuContent>
        </DropdownMenu>
    );
};

interface AddAttendeeDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    eventId: number;
    eventAttendees: AttendeeResponse[];
}

export function AddAttendeeDialog({open, onOpenChange, eventId, eventAttendees}: AddAttendeeDialogProps) {
    const [searchQuery, setSearchQuery] = useState("");
    const debouncedSearchQuery = useDebounce(searchQuery, 300);
    const [rowSelection, setRowSelection] = useState({});
    const [activeFilters, setActiveFilters] = useState<Record<string, string[]>>({});

    const {attendees: allAttendees, isLoadingAttendees} = useAttendees(0, 1000);
    const {addAttendee, isAddingAttendee} = useEventDetails(eventId);
    const {definitions: customFieldDefinitions, isLoading: isLoadingCustomFields} = useCustomFields();

    const columns = useMemo(() => getColumns(customFieldDefinitions), [customFieldDefinitions]);

    const availableAttendees = useMemo(() => {
        return allAttendees.filter(
            (attendee) => !eventAttendees.some(eventAttendee => eventAttendee.id === attendee.id)
        );
    }, [allAttendees, eventAttendees]);

    const filteredAttendees = useMemo(() => {
        let attendees = availableAttendees;

        if (debouncedSearchQuery) {
            attendees = attendees.filter(attendee =>
                `${attendee.firstName} ${attendee.lastName} ${attendee.uniqueIdentifier}`
                    .toLowerCase()
                    .includes(debouncedSearchQuery.toLowerCase())
            );
        }

        const filterKeys = Object.keys(activeFilters).filter(key => activeFilters[key].length > 0);
        if (filterKeys.length > 0) {
            attendees = attendees.filter(attendee => {
                return filterKeys.every(field => {
                    const selectedValues = activeFilters[field];
                    const attendeeValue = attendee.customFields?.[field];
                    return selectedValues.includes(String(attendeeValue));
                });
            });
        }

        return attendees;
    }, [availableAttendees, debouncedSearchQuery, activeFilters]);

    const table = useReactTable({
        data: filteredAttendees,
        columns,
        state: {rowSelection},
        getRowId: (row) => String(row.id),
        enableRowSelection: true,
        onRowSelectionChange: setRowSelection,
        getCoreRowModel: getCoreRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
    });

    useEffect(() => {
        if (!open) {
            setSearchQuery("");
            setActiveFilters({});
            setRowSelection({});
        }
    }, [open]);

    useEffect(() => {
        setRowSelection(currentSelection => {
            const visibleRowIds = new Set(filteredAttendees.map(a => String(a.id)));
            const newSelection: Record<string, boolean> = {};

            for (const id in currentSelection) {
                if (visibleRowIds.has(id)) {
                    newSelection[id] = true;
                }
            }
            return newSelection;
        });
    }, [filteredAttendees]);

    const handleAddSelected = async () => {
        const selectedIds = Object.keys(rowSelection).map(Number);
        if (selectedIds.length === 0) return;

        let successCount = 0;
        for (const attendeeId of selectedIds) {
            try {
                await new Promise<void>((resolve, reject) => {
                    addAttendee({eventId, attendeeId}, {onSuccess: () => resolve(), onError: (e) => reject(e)});
                });
                successCount++;
            } catch {
                toast.error(`Failed to add attendee with ID ${attendeeId}.`);
                break;
            }
        }

        if (successCount > 0) {
            toast.success(`${successCount} attendee(s) added successfully!`);
        }

        onOpenChange(false);
    };

    const handleClearFilters = () => {
        setSearchQuery("");
        setActiveFilters({});
    };

    const isFiltered = useMemo(() => {
        return searchQuery !== "" || Object.values(activeFilters).some(v => v.length > 0);
    }, [searchQuery, activeFilters]);

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-4xl max-h-[90vh] flex flex-col">
                <DialogHeader>
                    <DialogTitle>Add Attendees to Roster</DialogTitle>
                    <DialogDescription>
                        Select one or more attendees from the list below to add them to the event.
                    </DialogDescription>
                </DialogHeader>

                <div className="space-y-4 flex-grow flex flex-col min-h-0">
                    <div className="flex flex-col sm:flex-row gap-2">
                        <Input
                            placeholder="Search by name or identifier..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="h-9 max-w-sm"
                        />
                        <div className="flex items-center gap-2 flex-wrap">
                            {isLoadingCustomFields ? <Skeleton className="h-8 w-24"/> :
                                customFieldDefinitions.map(def =>
                                    <FilterDropdown
                                        key={def.id}
                                        field={def.fieldName}
                                        attendees={availableAttendees}
                                        activeFilters={activeFilters}
                                        onFilterChange={setActiveFilters}
                                    />
                                )
                            }
                            {isFiltered && (
                                <Button
                                    variant="ghost"
                                    size="sm"
                                    className="h-8 text-muted-foreground"
                                    onClick={handleClearFilters}
                                >
                                    <IconFilterOff className="mr-2 h-4 w-4"/>
                                    Clear
                                </Button>
                            )}
                        </div>
                    </div>

                    <div className="rounded-md border flex-grow overflow-y-auto">
                        <Table>
                            <TableHeader className="bg-muted sticky top-0 z-10">
                                {table.getHeaderGroups().map(headerGroup => (
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
                                {isLoadingAttendees ? (
                                    <TableRow>
                                        <TableCell colSpan={columns.length} className="h-24 text-center">Loading
                                            available
                                            attendees...</TableCell>
                                    </TableRow>
                                ) : table.getRowModel().rows.length > 0 ? (
                                    table.getRowModel().rows.map(row => (
                                        <TableRow key={row.id} data-state={row.getIsSelected() && "selected"}>
                                            {row.getVisibleCells().map(cell => (
                                                <TableCell key={cell.id}>
                                                    {flexRender(cell.column.columnDef.cell, cell.getContext())}
                                                </TableCell>
                                            ))}
                                        </TableRow>
                                    ))
                                ) : (
                                    <TableRow>
                                        <TableCell colSpan={columns.length} className="h-24 text-center">No attendees
                                            found.</TableCell>
                                    </TableRow>
                                )}
                            </TableBody>
                        </Table>
                    </div>
                    <DataTablePagination table={table}/>
                </div>
                <DialogFooter className="pt-4 border-t">
                    <Button variant="outline" onClick={() => onOpenChange(false)}>Cancel</Button>
                    <Button onClick={handleAddSelected}
                            disabled={isAddingAttendee || Object.keys(rowSelection).length === 0}>
                        {isAddingAttendee ? "Adding..." : `Add ${Object.keys(rowSelection).length} Attendee(s)`}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
