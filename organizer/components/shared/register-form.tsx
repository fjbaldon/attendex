"use client";

import {useForm} from "react-hook-form";
import {z} from "zod";
import {zodResolver} from "@hookform/resolvers/zod";
import {registerSchema} from "@/lib/schemas";
import {useAuth} from "@/hooks/use-auth";
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from "@/components/ui/form";
import {cn} from "@/lib/utils";
import {Button} from "@/components/ui/button";
import {Card, CardContent} from "@/components/ui/card";
import {Input} from "@/components/ui/input";
import Link from "next/link";
import {Ticket} from "lucide-react";
import Image from "next/image";

export function RegisterForm({
                                 className,
                                 ...props
                             }: React.ComponentProps<"div">) {
    const {register, isRegistering} = useAuth();

    const form = useForm({
        resolver: zodResolver(registerSchema),
        defaultValues: {
            organizationName: "",
            email: "",
            password: "",
        },
    });

    function onSubmit(values: z.infer<typeof registerSchema>) {
        register(values);
    }

    return (
        <div className={cn("flex flex-col gap-6", className)} {...props}>
            <Card className="overflow-hidden p-0 shadow-lg">
                <CardContent className="grid p-0 md:grid-cols-2">
                    <div className="flex flex-col justify-center p-6 md:p-8">
                        <Form {...form}>
                            <form onSubmit={form.handleSubmit(onSubmit)} className="flex flex-col gap-6">
                                <div className="flex flex-col items-center text-center gap-2">
                                    <div className="flex items-center gap-2 self-center font-semibold text-lg">
                                        <div
                                            className="bg-primary text-primary-foreground flex size-7 items-center justify-center rounded-md">
                                            <Ticket className="size-5"/>
                                        </div>
                                        AttendEx
                                    </div>
                                    <h1 className="text-2xl font-bold">Create an Account</h1>
                                    <p className="text-muted-foreground text-balance">
                                        Enter your details to get started.
                                    </p>
                                </div>

                                <FormField
                                    control={form.control}
                                    name="organizationName"
                                    render={({field}) => (
                                        <FormItem className="grid gap-2 text-left">
                                            <FormLabel>Organization Name</FormLabel>
                                            <FormControl>
                                                <Input placeholder="e.g., Northwood University" {...field} />
                                            </FormControl>
                                            <FormMessage/>
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="email"
                                    render={({field}) => (
                                        <FormItem className="grid gap-2 text-left">
                                            <FormLabel>Your Email</FormLabel>
                                            <FormControl>
                                                <Input type="email" placeholder="m@example.com" {...field} />
                                            </FormControl>
                                            <FormMessage/>
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="password"
                                    render={({field}) => (
                                        <FormItem className="grid gap-2 text-left">
                                            <FormLabel>Password</FormLabel>
                                            <FormControl>
                                                <Input type="password" placeholder="••••••••" {...field} />
                                            </FormControl>
                                            <FormMessage/>
                                        </FormItem>
                                    )}
                                />
                                <Button type="submit" className="w-full" disabled={isRegistering}>
                                    {isRegistering ? "Creating Account..." : "Create Account"}
                                </Button>

                                <div className="mt-4 text-center text-sm">
                                    Already have an account?{" "}
                                    <Link href="/login" className="underline underline-offset-4">
                                        Sign in
                                    </Link>
                                </div>
                            </form>
                        </Form>
                    </div>
                    <div className="bg-muted relative hidden md:block">
                        <Image
                            src="https://images.unsplash.com/photo-1522202176988-66273c2fd55f?q=80&w=2940&auto=format&fit=crop"
                            alt="A group of people working together on laptops"
                            fill
                            className="absolute inset-0 h-full w-full object-cover dark:brightness-[0.3]"
                        />
                    </div>
                </CardContent>
            </Card>
            <div
                className="text-muted-foreground *:[a]:hover:text-primary text-center text-xs text-balance *:[a]:underline *:[a]:underline-offset-4">
                By clicking continue, you agree to our <a href="#">Terms of Service</a>{" "}
                and <a href="#">Privacy Policy</a>.
            </div>
        </div>
    )
}
