import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {RecentEventStats} from "@/types";
import {Skeleton} from "@/components/ui/skeleton";
import Link from "next/link";
import {Badge} from "@/components/ui/badge";

interface RecentEventsStatsProps {
    events?: RecentEventStats[];
    isLoading: boolean;
}

const calculateAttendanceRate = (rosterCount: number, entryCount: number): string => {
    if (rosterCount === 0) return "0%";
    return `${((entryCount / rosterCount) * 100).toFixed(0)}%`;
};

export function RecentEventsStats({events, isLoading}: RecentEventsStatsProps) {
    return (
        <Card>
            <CardHeader>
                <CardTitle>Recent Event Performance</CardTitle>
                <CardDescription>A summary of attendance for your last few events.</CardDescription>
            </CardHeader>
            <CardContent>
                {isLoading ? (
                    <div className="space-y-4">
                        <Skeleton className="h-10 w-full"/>
                        <Skeleton className="h-10 w-full"/>
                        <Skeleton className="h-10 w-full"/>
                    </div>
                ) : !events || events.length === 0 ? (
                    <div className="flex flex-col items-center justify-center h-40 text-center">
                        <p className="text-sm font-medium">No recently completed events.</p>
                        <p className="text-sm text-muted-foreground">Data will appear here after events conclude.</p>
                    </div>
                ) : (
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>Event</TableHead>
                                <TableHead className="text-center">Attendance</TableHead>
                                <TableHead className="text-right">Rate</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {events.map((event) => (
                                <TableRow key={event.id}>
                                    <TableCell className="font-medium">
                                        <Link href={`/dashboard/events/${event.id}`}
                                              className="hover:underline underline-offset-4">
                                            {event.eventName}
                                        </Link>
                                    </TableCell>
                                    <TableCell className="text-center text-muted-foreground text-sm">
                                        {event.entryCount} / {event.rosterCount}
                                    </TableCell>
                                    <TableCell className="text-right">
                                        <Badge variant="secondary" className="font-semibold">
                                            {calculateAttendanceRate(event.rosterCount, event.entryCount)}
                                        </Badge>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                )}
            </CardContent>
        </Card>
    );
}
