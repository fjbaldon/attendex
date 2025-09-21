import {useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import * as z from "zod";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage} from "@/components/ui/form";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
    DialogFooter,
    DialogClose
} from "@/components/ui/dialog";

const formSchema = z.object({
    username: z.string().min(3, "Username must be at least 3 characters"),
    password: z.string().min(8, "Password must be at least 8 characters"),
});

interface ScannerDialogProps {
    onSave: (values: z.infer<typeof formSchema>) => void;
    children: React.ReactNode;
    open: boolean;
    onOpenChange: (open: boolean) => void;
}

export function ScannerDialog({onSave, children, open, onOpenChange}: ScannerDialogProps) {
    const form = useForm({
        resolver: zodResolver(formSchema),
        defaultValues: {username: "", password: ""},
    });

    const handleSubmit = (values: z.infer<typeof formSchema>) => {
        onSave(values);
        onOpenChange(false);
        form.reset();
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogTrigger asChild>{children}</DialogTrigger>
            <DialogContent className="sm:max-w-[425px]">
                <DialogHeader><DialogTitle>Create Scanner</DialogTitle></DialogHeader>
                <Form {...form}>
                    <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-4">
                        <FormField control={form.control} name="username" render={({field}) => (
                            <FormItem>
                                <FormLabel>Username</FormLabel>
                                <FormControl><Input {...field} /></FormControl>
                                <FormMessage/>
                            </FormItem>
                        )}/>
                        <FormField control={form.control} name="password" render={({field}) => (
                            <FormItem>
                                <FormLabel>Password</FormLabel>
                                <FormControl><Input type="password" {...field} /></FormControl>
                                <FormMessage/>
                            </FormItem>
                        )}/>
                        <DialogFooter>
                            <DialogClose asChild><Button type="button" variant="secondary">Cancel</Button></DialogClose>
                            <Button type="submit">Save</Button>
                        </DialogFooter>
                    </form>
                </Form>
            </DialogContent>
        </Dialog>
    );
}
