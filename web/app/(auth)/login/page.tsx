"use client";

import {LoginForm} from "@/components/shared/login-form";
import {useEffect, useRef, Suspense} from "react";
import {useSearchParams, useRouter} from "next/navigation";
import {toast} from "sonner";

// Extract logic that relies on client-side search params into its own component
function LoginContent() {
    const searchParams = useSearchParams();
    const router = useRouter();
    const toastShownRef = useRef(false);

    useEffect(() => {
        const sessionExpired = searchParams.get("sessionExpired");

        if (sessionExpired && !toastShownRef.current) {
            setTimeout(() => {
                toast.error("Session Expired", {
                    description: "You have been logged out. Please sign in again.",
                });
            }, 100);

            toastShownRef.current = true;

            // Remove the query param so the toast doesn't appear on refresh
            router.replace('/login', {scroll: false});
        }
    }, [searchParams, router]);

    return (
        <div className="w-full max-w-sm md:max-w-3xl">
            <LoginForm/>
        </div>
    );
}

export default function LoginPage() {
    return (
        <div className="bg-muted flex min-h-svh flex-col items-center justify-center p-6 md:p-10">
            {/* Wrap the component using useSearchParams in Suspense */}
            <Suspense fallback={
                <div className="w-full max-w-sm md:max-w-3xl opacity-50">
                    <LoginForm/>
                </div>
            }>
                <LoginContent />
            </Suspense>
        </div>
    );
}
