import {useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import * as z from "zod";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage} from "@/components/ui/form";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import api from "@/lib/api";
import {toast} from "sonner";
import {Link, useNavigate} from "react-router-dom";

const formSchema = z.object({
    username: z.string().min(3, "Username must be at least 3 characters"),
    password: z.string().min(8, "Password must be at least 8 characters"),
});
const RegisterPage = () => {
    const navigate = useNavigate();
    const form = useForm({
        resolver: zodResolver(formSchema),
        defaultValues: {username: "", password: ""},
    });
    const onSubmit = async (values: z.infer<typeof formSchema>) => {
        try {
            await api.post('/auth/register', values);
            toast.success("Registration successful! Please log in.");
            navigate('/login');
        } catch (error) {
            toast.error("Registration failed. Username might already be taken.");
        }
    };
    return (
        <div className="flex items-center justify-center min-h-screen bg-gray-100">
            <Card className="w-full max-w-sm">
                <CardHeader>
                    <CardTitle>Register</CardTitle>
                </CardHeader>
                <CardContent>
                    <Form {...form}>
                        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                            <FormField
                                control={form.control}
                                name="username"
                                render={({field}) => (
                                    <FormItem>
                                        <FormLabel>Username</FormLabel>
                                        <FormControl><Input placeholder="Create a username" {...field} /></FormControl>
                                        <FormMessage/>
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={form.control}
                                name="password"
                                render={({field}) => (
                                    <FormItem>
                                        <FormLabel>Password</FormLabel>
                                        <FormControl><Input type="password"
                                                            placeholder="Create a password" {...field} /></FormControl>
                                        <FormMessage/>
                                    </FormItem>
                                )}
                            />
                            <Button type="submit" className="w-full">Register</Button>
                        </form>
                    </Form>
                    <p className="text-center text-sm text-gray-600 mt-4">
                        Already have an account? <Link to="/login"
                                                       className="font-semibold text-blue-600 hover:underline">Login</Link>
                    </p>
                </CardContent>
            </Card>
        </div>
    );
};

export default RegisterPage;
