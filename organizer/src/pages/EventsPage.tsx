import {useState} from "react";
import {useQuery, useMutation, useQueryClient} from "@tanstack/react-query";
import api from "@/lib/api";
import {Button} from "@/components/ui/button";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger} from "@/components/ui/dropdown-menu";
import {MoreHorizontal} from "lucide-react";
import {toast} from "sonner";
import type {Event} from "@/types";
import {EventDialog} from "@/components/features/events/EventDialog";
import {useNavigate} from "react-router-dom";
import {Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogClose} from "@/components/ui/dialog";

const EventsPage = () => {
    const queryClient = useQueryClient();
    const navigate = useNavigate();
    const [isCreateDialogOpen, setCreateDialogOpen] = useState(false);
    const [editingEvent, setEditingEvent] = useState<Event | undefined>(undefined);
    const [deletingEvent, setDeletingEvent] = useState<Event | undefined>(undefined);

    const {data: events, isLoading} = useQuery<Event[]>({
        queryKey: ['events'],
        queryFn: () => api.get('/events').then(res => res.data)
    });

    const createMutation = useMutation({
        mutationFn: (newEvent: Omit<Event, 'id'>) => api.post('/events', newEvent),
        onSuccess: async () => {
            toast.success("Event created successfully!");
            await queryClient.invalidateQueries({queryKey: ['events']});
        },
        onError: () => toast.error("Failed to create event."),
    });

    const updateMutation = useMutation({
        mutationFn: (updatedEvent: Event) => api.put(`/events/${updatedEvent.id}`, updatedEvent),
        onSuccess: async () => {
            toast.success("Event updated successfully!");
            await queryClient.invalidateQueries({queryKey: ['events']});
        },
        onError: () => toast.error("Failed to update event."),
    });

    const deleteMutation = useMutation({
        mutationFn: (eventId: number) => api.delete(`/events/${eventId}`),
        onSuccess: async () => {
            toast.success("Event deleted successfully!");
            setDeletingEvent(undefined);
            await queryClient.invalidateQueries({queryKey: ['events']});
        },
        onError: () => toast.error("Failed to delete event."),
    });

    if (isLoading) return <div>Loading events...</div>;

    return (
        <div>
            <div className="flex justify-between items-center mb-4">
                <h1 className="text-3xl font-bold">Events</h1>
                <EventDialog open={isCreateDialogOpen} onOpenChange={setCreateDialogOpen}
                             onSave={(values) => createMutation.mutate(values)}>
                    <Button>Create Event</Button>
                </EventDialog>
            </div>
            <div className="border rounded-md">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Event Name</TableHead>
                            <TableHead>Start Date</TableHead>
                            <TableHead>End Date</TableHead>
                            <TableHead className="text-right">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {events?.map((event: Event) => (
                            <TableRow key={event.id}>
                                <TableCell>{event.eventName}</TableCell>
                                <TableCell>{event.startDate}</TableCell>
                                <TableCell>{event.endDate}</TableCell>
                                <TableCell className="text-right">
                                    <DropdownMenu>
                                        <DropdownMenuTrigger asChild>
                                            <Button variant="ghost" className="h-8 w-8 p-0"><MoreHorizontal
                                                className="h-4 w-4"/></Button>
                                        </DropdownMenuTrigger>
                                        <DropdownMenuContent align="end">
                                            <DropdownMenuItem onClick={() => navigate(`/events/${event.id}`)}>View
                                                Details</DropdownMenuItem>
                                            <DropdownMenuItem
                                                onClick={() => setEditingEvent(event)}>Edit</DropdownMenuItem>
                                            <DropdownMenuItem onClick={() => setDeletingEvent(event)}
                                                              className="text-red-600">Delete</DropdownMenuItem>
                                        </DropdownMenuContent>
                                    </DropdownMenu>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </div>

            {editingEvent && (
                <EventDialog
                    event={editingEvent}
                    open={!!editingEvent}
                    onOpenChange={(open) => !open && setEditingEvent(undefined)}
                    onSave={(values) => updateMutation.mutate({...values, id: editingEvent.id})}
                >
                    <></>
                </EventDialog>
            )}

            {deletingEvent && (
                <Dialog open={!!deletingEvent} onOpenChange={(open) => !open && setDeletingEvent(undefined)}>
                    <DialogContent>
                        <DialogHeader>
                            <DialogTitle>Are you sure?</DialogTitle>
                        </DialogHeader>
                        <p>This action cannot be undone. This will permanently delete the event
                            "{deletingEvent.eventName}".</p>
                        <DialogFooter>
                            <DialogClose asChild><Button variant="outline">Cancel</Button></DialogClose>
                            <Button variant="destructive"
                                    onClick={() => deleteMutation.mutate(deletingEvent.id)}>Delete</Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>
            )}
        </div>
    );
};

export default EventsPage;
