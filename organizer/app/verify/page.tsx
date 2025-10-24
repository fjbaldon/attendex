"use client";

import {Suspense, useEffect} from 'react';
import {useSearchParams} from 'next/navigation';
import {useMutation} from '@tanstack/react-query';
import api from '@/lib/api';
import {Card, CardContent} from '@/components/ui/card';
import {Button} from '@/components/ui/button';
import {CircleCheck, CircleX, LoaderCircle} from 'lucide-react';
import Link from 'next/link';
import {getErrorMessage} from "@/lib/utils";
import {AxiosError} from "axios";
import {ApiErrorResponse} from "@/types";

interface StatusContent {
    icon: React.ReactNode;
    title: string;
    description: string;
    button?: React.ReactNode;
}

function VerificationComponent() {
    const searchParams = useSearchParams();
    const token = searchParams.get('token');

    const {
        mutate,
        isPending,
        isSuccess,
        isError,
        data,
        error
    } = useMutation<string, AxiosError<ApiErrorResponse>, string>({
        mutationFn: async (verificationToken: string) => {
            const response = await api.get(`/api/v1/auth/verify?token=${verificationToken}`);
            return response.data;
        },
    });

    useEffect(() => {
        if (token) {
            mutate(token);
        }
    }, [token, mutate]);

    let content: StatusContent;

    if (!token) {
        content = {
            icon: <CircleX className="size-8 text-destructive"/>,
            title: "Verification Link Invalid",
            description: "No verification token was found in the URL. Please check the link from your email and try again.",
            button: <Button asChild className="mt-6 w-full"><Link href="/login">Return to Login</Link></Button>
        };
    } else if (isPending) {
        content = {
            icon: <LoaderCircle className="size-8 animate-spin text-muted-foreground"/>,
            title: "Verifying Your Account...",
            description: "Please wait a moment while we confirm your email address. This shouldn't take long."
        };
    } else if (isSuccess) {
        content = {
            icon: <CircleCheck className="size-8 text-primary"/>,
            title: "Account Verified!",
            description: data || "Your account has been successfully activated. You can now log in to access your dashboard.",
            button: <Button asChild className="mt-6 w-full"><Link href="/login">Proceed to Login</Link></Button>
        };
    } else {
        const descriptionMessage = error
            ? getErrorMessage(error, "This verification link may be invalid or has expired.")
            : "An unexpected error occurred. The link may be invalid or has expired.";

        content = {
            icon: <CircleX className="size-8 text-destructive"/>,
            title: "Verification Failed",
            description: descriptionMessage,
            button: <Button asChild variant="secondary" className="mt-6 w-full"><Link href="/register">Try Registering
                Again</Link></Button>
        };
    }

    return (
        <div className="bg-muted flex min-h-svh flex-col items-center justify-center p-6 md:p-10">
            <Card className="w-full max-w-md shadow-lg">
                <CardContent className="flex flex-col items-center p-8 text-center">
                    <div className="mb-4">
                        {content.icon}
                    </div>
                    <h1 className="text-2xl font-bold">{content.title}</h1>
                    <p className="text-muted-foreground mt-2 text-balance">
                        {content.description}
                    </p>
                    {content.button}
                </CardContent>
            </Card>
        </div>
    );
}

export default function VerifyPage() {
    return (
        <Suspense fallback={
            <div className="bg-muted flex min-h-svh flex-col items-center justify-center">
                <LoaderCircle className="size-8 animate-spin text-muted-foreground"/>
                <p className="text-muted-foreground mt-2">Loading verification page...</p>
            </div>
        }>
            <VerificationComponent/>
        </Suspense>
    );
}
