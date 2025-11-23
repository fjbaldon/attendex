import Link from 'next/link';
import {Button} from '@/components/ui/button';
import {Separator} from '@/components/ui/separator';
import {MailCheck} from 'lucide-react';
import {StatusCardLayout} from "@/components/shared/status-card-layout";

export default function RegisterSuccessPage() {
    return (
        <StatusCardLayout
            imageUrl="https://images.unsplash.com/photo-1516387938699-a93567ec168e?q=80&w=2940&auto=format&fit=crop"
            imageAlt="Person checking their email on a laptop"
        >
            <div className="bg-primary/10 text-primary mb-4 flex size-14 items-center justify-center rounded-full">
                <MailCheck className="size-8"/>
            </div>

            <h1 className="text-2xl font-bold">Check Your Inbox</h1>
            <p className="text-muted-foreground mt-2 text-balance">
                We&#39;ve sent a verification link to your email. Please click the link inside to activate your
                account.
            </p>

            <Separator className="my-6"/>

            <div className="flex w-full flex-col items-center gap-4">
                <Button asChild className="w-full">
                    <Link href="/login">Back to Login</Link>
                </Button>
                <p className="text-muted-foreground text-center text-xs">
                    Didn&#39;t receive it? Check your spam folder.
                </p>
            </div>
        </StatusCardLayout>
    );
}
