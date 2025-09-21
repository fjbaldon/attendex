import {useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import * as z from "zod";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage} from "@/components/ui/form";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {useAuth} from "@/contexts/AuthContext";
import api from "@/lib/api";
import {toast} from "sonner";
import {Link, useNavigate} from "react-router-dom";

const formSchema = z.object({
    username: z.string().min(1, "Username is required"),
    password: z.string().min(1, "Password is required"),
});

const LoginPage = () => {
    const {login} = useAuth();
    const navigate = useNavigate();

    const form = useForm({
        resolver: zodResolver(formSchema),
        defaultValues: {username: "", password: ""},
    });

    const onSubmit = async (values: z.infer<typeof formSchema>) => {
        try {
            const response = await api.post('/auth/login', values);
            login(response.data.accessToken);
            toast.success("Login successful!");
            navigate('/');
        } catch (error) {
            toast.error("Login failed. Please check your credentials.");
        }
    };

    return (
        <div className="flex items-center justify-center min-h-screen bg-gray-100">
            <Card className="w-full max-w-sm">
                <CardHeader>
                    <CardTitle>Login</CardTitle>
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
                                        <FormControl><Input placeholder="Your username" {...field} /></FormControl>
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
                                                            placeholder="Your password" {...field} /></FormControl>
                                        <FormMessage/>
                                    </FormItem>
                                )}
                            />
                            <Button type="submit" className="w-full">Login</Button>
                        </form>
                    </Form>
                    <p className="text-center text-sm text-gray-600 mt-4">
                        Don't have an account? <Link to="/register"
                                                     className="font-semibold text-blue-600 hover:underline">Register</Link>
                    </p>
                </CardContent>
            </Card>
        </div>
    );
};

export default LoginPage;
