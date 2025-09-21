import {useParams} from "react-router-dom";
import {useQuery, useMutation, useQueryClient} from "@tanstack/react-query";
import api from "@/lib/api";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer} from 'recharts';
import type {Event, EventAnalytics, Attendee} from "@/types";
import {toast} from "sonner";

const EventDetailsPage = () => {
    const {id} = useParams<{ id: string }>();
    const eventId = Number(id);
    const queryClient = useQueryClient();

    const {data: event, isLoading: isLoadingEvent} = useQuery<Event>({
        queryKey: ['event', eventId],
        queryFn: () => api.get(`/events/${eventId}`).then(res => res.data),
        enabled: !!eventId,
    });

    const {data: analytics, isLoading: isLoadingAnalytics} = useQuery<EventAnalytics>({
        queryKey: ['eventAnalytics', eventId],
        queryFn: () => api.get(`/reports/events/${eventId}/analytics`).then(res => res.data),
        enabled: !!eventId,
    });

    const {data: eventAttendees, isLoading: isLoadingEventAttendees} = useQuery<Attendee[]>({
        queryKey: ['eventAttendees', eventId],
        queryFn: () => api.get(`/events/${eventId}/attendees`).then(res => res.data),
        enabled: !!eventId,
    });

    const removeAttendeeMutation = useMutation({
        mutationFn: (attendeeId: number) => api.delete(`/events/${eventId}/attendees/${attendeeId}`),
        onSuccess: async () => {
            toast.success("Attendee removed from event.");
            await Promise.all([
                queryClient.invalidateQueries({queryKey: ['eventAttendees', eventId]}),
                queryClient.invalidateQueries({queryKey: ['eventAnalytics', eventId]})
            ]);
        },
        onError: () => toast.error("Failed to remove attendee."),
    });

    const chartData = analytics?.checkInsByDate
        ? Object.entries(analytics.checkInsByDate).map(([date, count]) => ({date, count: Number(count)}))
        : [];

    const isLoading = isLoadingEvent || isLoadingAnalytics || isLoadingEventAttendees;

    if (isLoading) return <div>Loading event details...</div>;
    if (!event || !analytics) return <div>Event not found.</div>;

    return (
        <div className="space-y-6">
            <h1 className="text-3xl font-bold mb-4">{event.eventName}</h1>

            <Card>
                <CardHeader><CardTitle>Event Analytics</CardTitle></CardHeader>
                <CardContent>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
                        <Card><CardHeader><CardTitle>Registered</CardTitle></CardHeader><CardContent><p
                            className="text-2xl font-bold">{analytics.totalRegistered}</p></CardContent></Card>
                        <Card><CardHeader><CardTitle>Checked-In</CardTitle></CardHeader><CardContent><p
                            className="text-2xl font-bold">{analytics.totalCheckedIn}</p></CardContent></Card>
                        <Card><CardHeader><CardTitle>Attendance Rate</CardTitle></CardHeader><CardContent><p
                            className="text-2xl font-bold">{analytics.attendanceRate.toFixed(2)}%</p>
                        </CardContent></Card>
                    </div>
                    <ResponsiveContainer width="100%" height={300}>
                        <BarChart data={chartData}><CartesianGrid strokeDasharray="3 3"/><XAxis dataKey="date"/><YAxis
                            allowDecimals={false}/><Tooltip/><Legend/><Bar dataKey="count" fill="#8884d8"
                                                                           name="Check-ins"/></BarChart>
                    </ResponsiveContainer>
                </CardContent>
            </Card>

            <Card>
                <CardHeader>
                    <div className="flex justify-between items-center">
                        <CardTitle>Registered Attendees</CardTitle>
                        {/* Add Attendee Dialog would go here */}
                    </div>
                </CardHeader>
                <CardContent>
                    <Table>
                        <TableHeader><TableRow><TableHead>ID Number</TableHead><TableHead>Name</TableHead><TableHead
                            className="text-right">Actions</TableHead></TableRow></TableHeader>
                        <TableBody>
                            {eventAttendees?.map(attendee => (
                                <TableRow key={attendee.id}>
                                    <TableCell>{attendee.schoolIdNumber}</TableCell>
                                    <TableCell>{`${attendee.lastName}, ${attendee.firstName}`}</TableCell>
                                    <TableCell className="text-right">
                                        <Button size="sm" variant="destructive"
                                                onClick={() => removeAttendeeMutation.mutate(attendee.id)}>Remove</Button>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </CardContent>
            </Card>
        </div>
    );
};

export default EventDetailsPage;
