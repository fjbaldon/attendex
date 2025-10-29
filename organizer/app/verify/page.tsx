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

interface FinalStatusContent {
    icon: React.ReactNode;
    title: string;
    description: string;
    button?: React.ReactNode;
    imageUrl: string;
    imageAlt: string;
}

function VerificationComponent() {
    const searchParams = useSearchParams();
    const token = searchParams.get('token');
    const hasRun = useRef(false);

    const {
        mutate,
        status, // Using the more descriptive 'status' field from react-query
        isSuccess,
        error,
    } = useMutation<string, AxiosError<ApiErrorResponse>, string>({
        mutationFn: async (verificationToken: string) => {
            const response = await api.get(`/api/v1/auth/verify?token=${verificationToken}`);
            return response.data;
        },
    });

    useEffect(() => {
        if (token && !hasRun.current) {
            hasRun.current = true;
            mutate(token);
        }
    }, [token, mutate]);

    // State 1: No token provided in the URL. This is a permanent error.
    if (!token) {
        return (
            <StatusCardLayout
                imageUrl="https://images.unsplash.com/photo-1555861496-0666c8981751?q=80&w=2940&auto=format&fit=crop"
                imageAlt="A broken chain link representing an invalid link"
            >
                <div className="mb-4"><Link2Off className="size-8 text-destructive"/></div>
                <h1 className="text-2xl font-bold">Invalid Link</h1>
                <p className="text-muted-foreground mt-2 text-balance">No verification token was found.</p>
                <Button asChild className="mt-6 w-full"><Link href="/login">Return to Login</Link></Button>
            </StatusCardLayout>
        );
    }

    // State 2: The mutation is either idle (pre-run) or pending (in-flight). This is the key fix.
    if (status === 'idle' || status === 'pending') {
        return <VerificationLoadingState/>;
    }

    // After this point, status is 'success' or 'error'. We can now safely determine the final UI.
    let finalContent: FinalStatusContent;

    if (isSuccess) {
        finalContent = {
            icon: <CircleCheck className="size-8 text-primary"/>,
            title: "Account Verified!",
            description: "Your account is now active. You can log in.",
            button: <Button asChild className="mt-6 w-full"><Link href="/login">Proceed to Login</Link></Button>,
            imageUrl: "https://images.unsplash.com/photo-1529333166437-7750a6dd5a70?q=80&w=2938&auto=format&fit=crop",
            imageAlt: "A person celebrating success with arms raised on a beach at sunset"
        };
    } else if (error?.response?.data?.message?.includes("already been verified")) {
        finalContent = {
            icon: <CircleCheck className="size-8 text-primary"/>,
            title: "Account Already Verified",
            description: "This account has already been confirmed. Please log in to continue.",
            button: <Button asChild className="mt-6 w-full"><Link href="/login">Proceed to Login</Link></Button>,
            imageUrl: "https://images.unsplash.com/photo-1529333166437-7750a6dd5a70?q=80&w=2938&auto=format&fit=crop",
            imageAlt: "A person celebrating success with arms raised on a beach at sunset"
        };
    } else if (error?.response?.data?.message?.includes("Invalid verification token")) {
        finalContent = {
            icon: <Link2Off className="size-8 text-destructive"/>,
            title: "Link Invalid or Expired",
            description: "This link may have expired or has already been used.",
            button: <Button asChild className="mt-6 w-full"><Link href="/login">Proceed to Login</Link></Button>,
            imageUrl: "https://images.unsplash.com/photo-1555861496-0666c8981751?q=80&w=2940&auto=format&fit=crop",
            imageAlt: "A broken chain link representing an invalid link"
        };
    } else {
        finalContent = {
            icon: <CircleX className="size-8 text-destructive"/>,
            title: "Verification Failed",
            description: getErrorMessage(error, "An unexpected error occurred."),
            button: <Button asChild variant="secondary" className="mt-6 w-full"><Link href="/register">Try Again</Link></Button>,
            imageUrl: "https://images.unsplash.com/photo-1584824486509-112e4181ff6b?q=80&w=2940&auto=format&fit=crop",
            imageAlt: "A computer screen showing an error message"
        };
    }

    return (
        <StatusCardLayout imageUrl={finalContent.imageUrl} imageAlt={finalContent.imageAlt}>
            <div className="mb-4">{finalContent.icon}</div>
            <h1 className="text-2xl font-bold">{finalContent.title}</h1>
            <p className="text-muted-foreground mt-2 text-balance">{finalContent.description}</p>
            {finalContent.button}
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
