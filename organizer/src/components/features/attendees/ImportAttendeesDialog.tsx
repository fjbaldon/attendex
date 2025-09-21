import React, {useState, useRef} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
    DialogFooter,
    DialogClose
} from "@/components/ui/dialog";
import api from '@/lib/api';
import {toast} from 'sonner';

export function ImportAttendeesDialog() {
    const queryClient = useQueryClient();
    const [file, setFile] = useState<File | null>(null);
    const [open, setOpen] = useState(false);
    const fileInputRef = useRef<HTMLInputElement>(null);

    const mutation = useMutation({
        mutationFn: (formData: FormData) => api.post('/attendees/import', formData, {
            headers: {'Content-Type': 'multipart/form-data'},
        }),
        onSuccess: async (res) => {
            const {successfulImports, failedImports} = res.data;
            toast.success(`Import complete! Successful: ${successfulImports}, Failed: ${failedImports}`);
            setOpen(false);
            setFile(null);
            await queryClient.invalidateQueries({queryKey: ['attendees']});
        },
        onError: (err: any) => {
            toast.error(`Import failed: ${err.response?.data?.message || 'Server error'}`);
        }
    });

    const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        if (event.target.files) {
            setFile(event.target.files[0]);
        }
    };

    const handleImport = () => {
        if (file) {
            const formData = new FormData();
            formData.append('file', file);
            mutation.mutate(formData);
        } else {
            toast.warning("Please select a file to import.");
        }
    };

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild><Button variant="outline" className="mr-2">Import CSV</Button></DialogTrigger>
            <DialogContent>
                <DialogHeader><DialogTitle>Import Attendees from CSV</DialogTitle></DialogHeader>
                <div className="grid gap-4 py-4">
                    <p className='text-sm text-muted-foreground'>
                        Select a CSV file with headers: schoolIdNumber, firstName, lastName, middleInitial, course,
                        yearLevel.
                    </p>
                    <Input type="file" accept=".csv" onChange={handleFileChange} ref={fileInputRef}/>
                </div>
                <DialogFooter>
                    <DialogClose asChild><Button variant="outline">Cancel</Button></DialogClose>
                    <Button onClick={handleImport} disabled={!file || mutation.isPending}>
                        {mutation.isPending ? "Importing..." : "Import"}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
