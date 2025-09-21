import {useState} from "react";
import {useQuery, useMutation, useQueryClient, keepPreviousData} from "@tanstack/react-query"; // 1. IMPORT `keepPreviousData`
import api from "@/lib/api";
import {Button} from "@/components/ui/button";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger} from "@/components/ui/dropdown-menu";
import {
    Pagination,
    PaginationContent,
    PaginationItem,
    PaginationNext,
    PaginationPrevious
} from "@/components/ui/pagination";
import {MoreHorizontal} from "lucide-react";
import {toast} from "sonner";
import type {Attendee, Page} from "@/types";
import {AttendeeDialog} from "@/components/features/attendees/AttendeeDialog";
import {ImportAttendeesDialog} from "@/components/features/attendees/ImportAttendeesDialog";
import {Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogClose} from "@/components/ui/dialog";

const AttendeesPage = () => {
    const queryClient = useQueryClient();
    const [page, setPage] = useState(0);
    const [isCreateDialogOpen, setCreateDialogOpen] = useState(false);
    const [editingAttendee, setEditingAttendee] = useState<Attendee | undefined>(undefined);
    const [deletingAttendee, setDeletingAttendee] = useState<Attendee | undefined>(undefined);

    const {data, isLoading} = useQuery<Page<Attendee>>({
        queryKey: ['attendees', page],
        queryFn: () => api.get(`/attendees?page=${page}&size=10`).then(res => res.data),
        placeholderData: keepPreviousData,
    });

    const createMutation = useMutation({
        mutationFn: (newAttendee: Omit<Attendee, 'id'>) => api.post('/attendees', newAttendee),
        onSuccess: async () => {
            toast.success("Attendee created!");
            await queryClient.invalidateQueries({queryKey: ['attendees']});
        },
        onError: () => toast.error("Failed to create attendee."),
    });

    const updateMutation = useMutation({
        mutationFn: (updated: Attendee) => api.put(`/attendees/${updated.id}`, updated),
        onSuccess: async () => {
            toast.success("Attendee updated!");
            await queryClient.invalidateQueries({queryKey: ['attendees']});
        },
        onError: () => toast.error("Failed to update attendee."),
    });

    const deleteMutation = useMutation({
        mutationFn: (id: number) => api.delete(`/attendees/${id}`),
        onSuccess: async () => {
            toast.success("Attendee deleted!");
            setDeletingAttendee(undefined);
            await queryClient.invalidateQueries({queryKey: ['attendees']});
        },
        onError: () => toast.error("Failed to delete attendee."),
    });

    if (isLoading && !data) return <div>Loading attendees...</div>; // Show loading only on the initial fetch

    return (
        <div>
            <div className="flex justify-between items-center mb-4">
                <h1 className="text-3xl font-bold">Attendees</h1>
                <div>
                    <ImportAttendeesDialog/>
                    <AttendeeDialog open={isCreateDialogOpen} onOpenChange={setCreateDialogOpen}
                                    onSave={(values) => createMutation.mutate(values)}>
                        <Button>Create Attendee</Button>
                    </AttendeeDialog>
                </div>
            </div>
            <div className="border rounded-md">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>ID Number</TableHead>
                            <TableHead>Name</TableHead>
                            <TableHead>Course</TableHead>
                            <TableHead>Year</TableHead>
                            <TableHead className="text-right">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {data?.content?.map((attendee) => (
                            <TableRow key={attendee.id}>
                                <TableCell>{attendee.schoolIdNumber}</TableCell>
                                <TableCell>{`${attendee.lastName}, ${attendee.firstName}`}</TableCell>
                                <TableCell>{attendee.course || 'N/A'}</TableCell>
                                <TableCell>{attendee.yearLevel || 'N/A'}</TableCell>
                                <TableCell className="text-right">
                                    <DropdownMenu>
                                        <DropdownMenuTrigger asChild><Button variant="ghost"
                                                                             className="h-8 w-8 p-0"><MoreHorizontal
                                            className="h-4 w-4"/></Button></DropdownMenuTrigger>
                                        <DropdownMenuContent align="end">
                                            <DropdownMenuItem
                                                onClick={() => setEditingAttendee(attendee)}>Edit</DropdownMenuItem>
                                            <DropdownMenuItem onClick={() => setDeletingAttendee(attendee)}
                                                              className="text-red-600">Delete</DropdownMenuItem>
                                        </DropdownMenuContent>
                                    </DropdownMenu>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </div>
            <Pagination className="mt-4">
                <PaginationContent>
                    <PaginationItem>
                        <PaginationPrevious
                            href="#"
                            onClick={(e) => {
                                e.preventDefault();
                                setPage(p => Math.max(0, p - 1));
                            }}
                            aria-disabled={page === 0}
                            className={page === 0 ? "pointer-events-none opacity-50" : ""}
                        />
                    </PaginationItem>
                    <PaginationItem>
                        <PaginationNext
                            href="#"
                            onClick={(e) => {
                                e.preventDefault();
                                if (data && page < data.totalPages - 1) setPage(p => p + 1);
                            }}
                            aria-disabled={!data || page >= data.totalPages - 1}
                            className={!data || page >= data.totalPages - 1 ? "pointer-events-none opacity-50" : ""}
                        />
                    </PaginationItem>
                </PaginationContent>
            </Pagination>

            {editingAttendee && (
                <AttendeeDialog attendee={editingAttendee} open={!!editingAttendee}
                                onOpenChange={(open) => !open && setEditingAttendee(undefined)}
                                onSave={(values) => updateMutation.mutate({...values, id: editingAttendee.id})}>
                    <></>
                </AttendeeDialog>
            )}

            {deletingAttendee && (
                <Dialog open={!!deletingAttendee} onOpenChange={(open) => !open && setDeletingAttendee(undefined)}>
                    <DialogContent>
                        <DialogHeader><DialogTitle>Are you sure?</DialogTitle></DialogHeader>
                        <p>This will permanently delete the attendee
                            "{deletingAttendee.firstName} {deletingAttendee.lastName}".</p>
                        <DialogFooter>
                            <DialogClose asChild><Button variant="outline">Cancel</Button></DialogClose>
                            <Button variant="destructive"
                                    onClick={() => deleteMutation.mutate(deletingAttendee.id)}>Delete</Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>
            )}
        </div>
    );
};

export default AttendeesPage;
