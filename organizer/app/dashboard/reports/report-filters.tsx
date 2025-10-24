"use client";
import * as React from 'react';
import {AttendeeResponse} from "@/types";
import {Button} from "@/components/ui/button";
import {
    DropdownMenu,
    DropdownMenuCheckboxItem,
    DropdownMenuContent,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger
} from "@/components/ui/dropdown-menu";
import {IconFilter, IconChevronDown} from "@tabler/icons-react";

interface ReportFiltersProps {
    attendees: AttendeeResponse[];
    customFields: string[];
    activeFilters: Record<string, string[]>;
    onFilterChange: (filters: Record<string, string[]>) => void;
}

const findValueCaseInsensitive = (obj: Record<string, any> | null | undefined, searchKey: string): any => {
    if (!obj) return undefined;
    const actualKey = Object.keys(obj).find(key => key.toLowerCase() === searchKey.toLowerCase());
    return actualKey ? obj[actualKey] : undefined;
};

export function ReportFilters({attendees, customFields, activeFilters, onFilterChange}: ReportFiltersProps) {
    const handleSelect = (field: string, value: string, isSelected: boolean) => {
        const currentSelection = activeFilters[field] || [];
        const newSelection = isSelected ? [...currentSelection, value] : currentSelection.filter(item => item !== value);
        onFilterChange({...activeFilters, [field]: newSelection});
    };

    if (!customFields || customFields.length === 0) {
        return <p className="text-sm text-muted-foreground">No custom fields have been configured for filtering.</p>;
    }

    return (
        <div className="flex items-center flex-wrap gap-2">
            <IconFilter className="h-4 w-4 text-muted-foreground" stroke={1.5}/>
            <p className="text-sm font-medium mr-2">Filter by:</p>
            {customFields.map(field => {
                const options = Array.from(
                    attendees.reduce((acc, attendee) => {
                        const value = findValueCaseInsensitive(attendee.customFields, field);
                        if (value != null && String(value).trim() !== "") {
                            acc.add(String(value));
                        }
                        return acc;
                    }, new Set<string>())
                ).sort();

                return (
                    <DropdownMenu key={field}>
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
                            {options.length > 0 ? (
                                options.map(option => (
                                    <DropdownMenuCheckboxItem key={option}
                                                              checked={activeFilters[field]?.includes(option)}
                                                              onCheckedChange={(isSelected) => handleSelect(field, option, !!isSelected)}>
                                        {option}
                                    </DropdownMenuCheckboxItem>
                                ))
                            ) : (
                                <div className="px-2 py-1.5 text-sm text-muted-foreground italic">
                                    No options available for this filter.
                                </div>
                            )}
                        </DropdownMenuContent>
                    </DropdownMenu>
                );
            })}
        </div>
    );
}
