import {useState} from "react";
import {useQuery, useMutation, useQueryClient} from "@tanstack/react-query";
import api from "@/lib/api";
import {Button} from "@/components/ui/button";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {toast} from "sonner";
import type {Scanner} from "@/types";
import {ScannerDialog} from "@/components/features/scanners/ScannerDialog";
import {Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogClose} from "@/components/ui/dialog";

const ScannersPage = () => {
    const queryClient = useQueryClient();
    const [isCreateDialogOpen, setCreateDialogOpen] = useState(false);
    const [deletingScanner, setDeletingScanner] = useState<Scanner | undefined>(undefined);

    const {data: scanners, isLoading} = useQuery<Scanner[]>({
        queryKey: ['scanners'],
        queryFn: () => api.get('/scanners').then(res => res.data)
    });

    const createMutation = useMutation({
        mutationFn: (newScanner: Omit<Scanner, 'id'>) => api.post('/scanners', newScanner),
        onSuccess: async () => {
            toast.success("Scanner created successfully!");
            await queryClient.invalidateQueries({queryKey: ['scanners']});
        },
        onError: () => toast.error("Failed to create scanner."),
    });

    const deleteMutation = useMutation({
        mutationFn: (scannerId: number) => api.delete(`/scanners/${scannerId}`),
        onSuccess: async () => {
            toast.success("Scanner deleted successfully!");
            setDeletingScanner(undefined);
            await queryClient.invalidateQueries({queryKey: ['scanners']});
        },
        onError: () => toast.error("Failed to delete scanner."),
    });

    if (isLoading) return <div>Loading scanners...</div>;

    return (
        <div>
            <div className="flex justify-between items-center mb-4">
                <h1 className="text-3xl font-bold">Scanners</h1>
                <ScannerDialog open={isCreateDialogOpen} onOpenChange={setCreateDialogOpen}
                               onSave={(values) => createMutation.mutate(values)}>
                    <Button>Create Scanner</Button>
                </ScannerDialog>
            </div>
            <div className="border rounded-md">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Username</TableHead>
                            <TableHead className="text-right">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {scanners?.map((scanner) => (
                            <TableRow key={scanner.id}>
                                <TableCell>{scanner.username}</TableCell>
                                <TableCell className="text-right">
                                    <Button variant="destructive" size="sm"
                                            onClick={() => setDeletingScanner(scanner)}>Delete</Button>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </div>
            {deletingScanner && (
                <Dialog open={!!deletingScanner} onOpenChange={(open) => !open && setDeletingScanner(undefined)}>
                    <DialogContent>
                        <DialogHeader><DialogTitle>Are you sure?</DialogTitle></DialogHeader>
                        <p>This will permanently delete the scanner "{deletingScanner.username}".</p>
                        <DialogFooter>
                            <DialogClose asChild><Button variant="outline">Cancel</Button></DialogClose>
                            <Button variant="destructive"
                                    onClick={() => deleteMutation.mutate(deletingScanner.id)}>Delete</Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>
            )}
        </div>
    );
};

export default ScannersPage;
