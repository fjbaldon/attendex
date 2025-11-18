import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {UpcomingEvent} from "@/types";
import {Skeleton} from "@/components/ui/skeleton";
import Link from "next/link";
import {format} from "date-fns";

interface UpcomingEventsProps {
    events?: UpcomingEvent[];
    isLoading: boolean;
}

export function UpcomingEvents({events, isLoading}: UpcomingEventsProps) {
    return (
        <Card className="h-full flex flex-col">
            <CardHeader>
                <CardTitle>Upcoming Events</CardTitle>
                <CardDescription>A look at what&#39;s coming next on your schedule.</CardDescription>
            </CardHeader>
            <CardContent className="flex-grow">
                {isLoading ? (
                    <div className="space-y-4">
                        {[...Array(3)].map((_, i) => <Skeleton key={i} className="h-10 w-full"/>)}
                    </div>
                ) : !events || events.length === 0 ? (
                    <div className="flex flex-col items-center justify-center h-full text-center">
                        <p className="text-sm font-medium">No upcoming events found.</p>
                        <p className="text-sm text-muted-foreground">Why not schedule a new one?</p>
                    </div>
                ) : (
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>Event</TableHead>
                                <TableHead className="text-right">Date</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {events.map((event) => (
                                <TableRow key={event.id}>
                                    <TableCell className="font-medium">
                                        <Link href={`/dashboard/events/${event.id}`}
                                              className="hover:underline underline-offset-4">
                                            {event.name}
                                        </Link>
                                    </TableCell>
                                    <TableCell className="text-right text-muted-foreground text-sm">
                                        {format(new Date(event.startDate), "MMM d, yyyy")}
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
