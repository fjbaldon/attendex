"use client";

import {Suspense, useEffect, useRef} from 'react';
import {useSearchParams} from 'next/navigation';
import {useMutation} from '@tanstack/react-query';
import api from '@/lib/api';
import {Button} from '@/components/ui/button';
import {CircleCheck, CircleX, Link2Off, LoaderCircle} from 'lucide-react';
import Link from 'next/link';
import {getErrorMessage} from "@/lib/utils";
import {AxiosError} from "axios";
import {ApiErrorResponse} from "@/types";
import {StatusCardLayout} from "@/components/shared/status-card-layout";
import {VerificationLoadingState} from "./loading-state";
import {Separator} from "@/components/ui/separator";

interface FinalStatusContent {
    icon: React.ReactNode;
    title: string;
    description: string;
    footer: React.ReactNode;
    imageUrl: string;
    imageAlt: string;
}

function VerificationComponent() {
    const searchParams = useSearchParams();
    const token = searchParams.get('token');
    const hasRun = useRef(false);

    const {
        mutate,
        status,
        isSuccess,
        error,
    } = useMutation<string, AxiosError<ApiErrorResponse>, string>({
        mutationFn: async (verificationToken: string) => {
            const response = await api.get(`/api/v1/organizations/verify?token=${verificationToken}`);
            return response.data;
        },
    });

    useEffect(() => {
        if (token && !hasRun.current) {
            hasRun.current = true;
            mutate(token);
        }
    }, [token, mutate]);

    if (!token) {
        return (
            <StatusCardLayout
                imageUrl="https://images.unsplash.com/photo-1555861496-0666c8981751?q=80&w=2940&auto=format&fit=crop"
                imageAlt="A broken chain link representing an invalid link"
            >
                <div className="mb-4"><Link2Off className="size-8 text-destructive"/></div>
                <h1 className="text-2xl font-bold">Invalid Link</h1>
                <p className="text-muted-foreground mt-2 text-balance">No verification token was found in the URL.</p>
                <Separator className="my-6"/>
                <div className="flex w-full flex-col items-center gap-4">
                    <Button asChild className="w-full"><Link href="/login">Return to Login</Link></Button>
                </div>
            </StatusCardLayout>
        );
    }

    if (status === 'idle' || status === 'pending') {
        return <VerificationLoadingState/>;
    }

    let finalContent: FinalStatusContent;

    if (isSuccess || error?.response?.data?.message?.includes("already been verified")) {
        finalContent = {
            icon: <CircleCheck className="size-8 text-primary"/>,
            title: isSuccess ? "Account Verified!" : "Account Already Verified",
            description: "Your account is active. You can now log in to access your dashboard.",
            footer: <Button asChild className="w-full"><Link href="/login">Proceed to Login</Link></Button>,
            imageUrl: "https://images.unsplash.com/photo-1529333166437-7750a6dd5a70?q=80&w=2938&auto=format&fit=crop",
            imageAlt: "A person celebrating success with arms raised on a beach at sunset"
        };
    } else if (error?.response?.data?.message?.includes("Invalid verification token")) {
        finalContent = {
            icon: <Link2Off className="size-8 text-destructive"/>,
            title: "Link Invalid or Expired",
            description: "This verification link may have expired or has already been used.",
            footer: (
                <>
                    <Button asChild className="w-full"><Link href="/login">Back to Login</Link></Button>
                    <p className="text-muted-foreground text-center text-xs">
                        If you just signed up, please check for a more recent email.
                    </p>
                </>
            ),
            imageUrl: "https://images.unsplash.com/photo-1555861496-0666c8981751?q=80&w=2940&auto=format&fit=crop",
            imageAlt: "A broken chain link representing an invalid link"
        };
    } else {
        finalContent = {
            icon: <CircleX className="size-8 text-destructive"/>,
            title: "Verification Failed",
            description: getErrorMessage(error, "An unexpected error occurred."),
            footer: <Button asChild variant="secondary" className="w-full"><Link
                href="/register">Register Again</Link></Button>,
            imageUrl: "https://images.unsplash.com/photo-1584824486509-112e4181ff6b?q=80&w=2940&auto=format&fit=crop",
            imageAlt: "A computer screen showing an error message"
        };
    }

    return (
        <StatusCardLayout imageUrl={finalContent.imageUrl} imageAlt={finalContent.imageAlt}>
            <div className="mb-4">{finalContent.icon}</div>
            <h1 className="text-2xl font-bold">{finalContent.title}</h1>
            <p className="text-muted-foreground mt-2 text-balance">{finalContent.description}</p>
            <Separator className="my-6"/>
            <div className="flex w-full flex-col items-center gap-4">
                {finalContent.footer}
            </div>
        </StatusCardLayout>
    );
}

export default function VerifyPage() {
    return (
        <Suspense fallback={
            <div className="bg-muted flex min-h-svh flex-col items-center justify-center">
                <LoaderCircle className="size-8 animate-spin text-muted-foreground"/>
                <p className="text-muted-foreground mt-2">Loading...</p>
            </div>
        }>
            <VerificationComponent/>
        </Suspense>
    );
}
