"use client";

import * as React from 'react';
import {AttendeeResponse} from "@/types";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {Separator} from '@/components/ui/separator';
import {Badge} from "@/components/ui/badge";

interface AttendeeRosterProps {
    eventName: string;
    organizationName: string;
    listTitle: string;
    totalAttendees: number;
    filteredAttendees: AttendeeResponse[];
    customFields: string[];
    activeFilters: Record<string, string[]>;
}

export const AttendeeRoster = React.forwardRef<HTMLDivElement, AttendeeRosterProps>(
    ({eventName, organizationName, listTitle, totalAttendees, filteredAttendees, customFields, activeFilters}, ref) => {
        const generationDate = new Date().toLocaleString();

        const activeFilterEntries = Object.entries(activeFilters).filter(([, values]) => values.length > 0);

        return (
            <div ref={ref} className="bg-background rounded-lg border p-8">
                {/* --- HEADER --- */}
                <div className="mb-6">
                    <h2 className="text-3xl font-bold">{eventName}</h2>
                    <p className="text-lg text-muted-foreground">{listTitle}</p>
                </div>

                {/* --- REPORT DETAILS --- */}
                <div className="grid grid-cols-3 gap-4 mb-6 text-sm">
                    <div className="space-y-1">
                        <p className="font-semibold">Total in List:</p>
                        <p className="text-muted-foreground">{totalAttendees}</p>
                    </div>
                    <div className="space-y-1">
                        <p className="font-semibold">Showing in this Report:</p>
                        <p className="text-muted-foreground">{filteredAttendees.length}</p>
                    </div>
                    <div className="space-y-1">
                        <p className="font-semibold">Filters Applied:</p>
                        <div className="flex flex-wrap gap-1">
                            {activeFilterEntries.length > 0 ? (
                                activeFilterEntries.map(([field, values]) => (
                                    <Badge key={field} variant="secondary" className="font-normal">
                                        <span className="font-semibold">{field}:</span>&nbsp;
                                        <span>{values.join(', ')}</span>
                                    </Badge>
                                ))
                            ) : (
                                <p className="text-muted-foreground">None</p>
                            )}
                        </div>
                    </div>
                </div>

                <Separator className="mb-6"/>

                {/* --- TABLE --- */}
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead className="w-[150px]">Identifier</TableHead>
                            <TableHead>Last Name</TableHead>
                            <TableHead>First Name</TableHead>
                            {customFields.map(field => <TableHead key={field}>{field}</TableHead>)}
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {filteredAttendees.length > 0 ? (
                            filteredAttendees.map(attendee => (
                                <TableRow key={attendee.id} className="odd:bg-muted/50">
                                    <TableCell className="font-mono text-xs">{attendee.uniqueIdentifier}</TableCell>
                                    <TableCell className="font-medium">{attendee.lastName}</TableCell>
                                    <TableCell>{attendee.firstName}</TableCell>
                                    {customFields.map(field => {
                                        const value = attendee.customFields?.[field];
                                        const displayValue = (value != null && value !== '')
                                            ? String(value)
                                            : <span className="text-muted-foreground italic text-xs">N/A</span>;
                                        return <TableCell key={field}>{displayValue}</TableCell>;
                                    })}
                                </TableRow>
                            ))
                        ) : (
                            <TableRow>
                                <TableCell colSpan={3 + customFields.length} className="h-24 text-center">
                                    No attendees match the current filter criteria.
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>

                {/* --- FOOTER --- */}
                <div className="mt-8 flex justify-between text-xs text-muted-foreground">
                    <span>{organizationName}</span>
                    <span>Report generated on: {generationDate}</span>
                </div>
            </div>
        );
    }
);

AttendeeRoster.displayName = "AttendeeRoster";
