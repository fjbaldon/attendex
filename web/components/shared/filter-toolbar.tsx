"use client";

import * as React from "react";
import {Input} from "@/components/ui/input";
import {Popover, PopoverContent, PopoverTrigger} from "@/components/ui/popover";
import {Button} from "@/components/ui/button";
import {IconFilter, IconX} from "@tabler/icons-react";
import {Badge} from "@/components/ui/badge";
import {Label} from "@/components/ui/label";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Attribute} from "@/types";

interface FilterToolbarProps {
    searchQuery: string;
    onSearchChange: (value: string) => void;
    searchPlaceholder?: string;
    activeFilters: Record<string, string>;
    onFiltersChange: (filters: Record<string, string>) => void;
    attributes: Attribute[];
    children?: React.ReactNode; // For additional actions like "Delete Selected" appended to the scrolling row
}

export function FilterToolbar({
                                  searchQuery,
                                  onSearchChange,
                                  searchPlaceholder = "Search...",
                                  activeFilters,
                                  onFiltersChange,
                                  attributes,
                                  children
                              }: FilterToolbarProps) {

    const updateFilter = (attribute: string, value: string | "ALL") => {
        const next = {...activeFilters};
        if (value === "ALL") {
            delete next[attribute];
        } else {
            next[attribute] = value;
        }
        onFiltersChange(next);
    };

    return (
        <div className="flex flex-1 items-center gap-2 overflow-x-auto p-2">
            <Input
                placeholder={searchPlaceholder}
                value={searchQuery}
                onChange={(event) => onSearchChange(event.target.value)}
                className="h-9 w-[200px] min-w-[150px]"
            />

            <Popover>
                <PopoverTrigger asChild>
                    <Button variant="outline" size="sm" className="h-9 border-dashed">
                        <IconFilter className="mr-2 h-4 w-4" />
                        Filters
                        {Object.keys(activeFilters).length > 0 && (
                            <Badge variant="secondary" className="ml-2 h-5 rounded-sm px-1 font-normal lg:hidden">
                                {Object.keys(activeFilters).length}
                            </Badge>
                        )}
                    </Button>
                </PopoverTrigger>
                <PopoverContent className="w-[280px] p-4" align="start">
                    <div className="space-y-4">
                        <div className="space-y-2">
                            <h4 className="font-medium leading-none">Filter Attributes</h4>
                            <p className="text-sm text-muted-foreground">
                                Narrow down the list by attribute.
                            </p>
                        </div>
                        <div className="grid gap-2">
                            {attributes.map((attr) => (
                                <div key={attr.id} className="grid grid-cols-3 items-center gap-4">
                                    <Label htmlFor={`filter-${attr.name}`} className="text-xs">{attr.name}</Label>
                                    <Select
                                        value={activeFilters[attr.name] || "ALL"}
                                        onValueChange={(val) => updateFilter(attr.name, val)}
                                    >
                                        <SelectTrigger id={`filter-${attr.name}`} className="col-span-2 h-8">
                                            <SelectValue placeholder="All" />
                                        </SelectTrigger>
                                        <SelectContent>
                                            <SelectItem value="ALL">All</SelectItem>
                                            {attr.options?.map((opt) => (
                                                <SelectItem key={opt} value={opt}>{opt}</SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>
                                </div>
                            ))}
                            {attributes.length === 0 && (
                                <p className="text-xs text-muted-foreground italic">No attributes defined.</p>
                            )}
                        </div>
                        {Object.keys(activeFilters).length > 0 && (
                            <Button
                                variant="ghost"
                                size="sm"
                                className="w-full h-8 font-normal"
                                onClick={() => onFiltersChange({})}
                            >
                                Clear filters
                            </Button>
                        )}
                    </div>
                </PopoverContent>
            </Popover>

            {Object.entries(activeFilters).map(([key, value]) => (
                <Badge key={key} variant="secondary" className="h-7 px-2 rounded-sm gap-1 font-normal">
                    {key}: {value}
                    <span
                        className="cursor-pointer hover:text-destructive flex items-center justify-center ml-1"
                        onClick={() => updateFilter(key, "ALL")}
                    >
                        <IconX className="h-3 w-3" />
                    </span>
                </Badge>
            ))}

            {children}
        </div>
    );
}
