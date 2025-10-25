import Link from 'next/link';
import {Button} from '@/components/ui/button';
import {Card, CardContent} from '@/components/ui/card';
import {MailCheck} from 'lucide-react';
import {Separator} from '@/components/ui/separator';

export default function RegisterSuccessPage() {
    return (
        <div className="bg-muted flex min-h-svh flex-col items-center justify-center p-6 md:p-10">
            <Card className="overflow-hidden p-0 shadow-lg w-full max-w-sm md:max-w-3xl">
                <CardContent className="grid p-0 md:grid-cols-2">
                    <div className="flex flex-col items-center justify-center p-6 text-center md:p-8">
                        <div
                            className="bg-primary/10 text-primary mb-4 flex size-14 items-center justify-center rounded-full">
                            <MailCheck className="size-8"/>
                        </div>
                        <h1 className="text-2xl font-bold">Verify Your Email</h1>
                        <p className="text-muted-foreground mt-2 text-balance">
                            We&#39;ve sent a verification link to your email address. Please click the link to activate
                            your
                            account and get started.
                        </p>
                        <Separator className="my-6"/>
                        <div className="flex w-full flex-col gap-2">
                            <Button asChild className="w-full">
                                <Link href="/login">Back to Login</Link>
                            </Button>
                            <p className="text-muted-foreground text-center text-xs">
                                Didn&#39;t receive it? Check your spam folder.
                            </p>
                        </div>
                    </div>
                    <div className="bg-muted relative hidden md:block">
                        <img
                            src="https://images.unsplash.com/photo-1516387938699-a93567ec168e?q=80&w=2940&auto=format&fit=crop"
                            alt="Person checking their email on a laptop"
                            className="absolute inset-0 h-full w-full object-cover dark:brightness-[0.3]"
                        />
                    </div>
                </CardContent>
            </Card>
        </div>
    );
}
