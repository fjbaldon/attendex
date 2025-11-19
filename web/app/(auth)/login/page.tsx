"use client";

import {LoginForm} from "@/components/shared/login-form";
import {useEffect, useRef} from "react";
import {useSearchParams, useRouter} from "next/navigation";
import {toast} from "sonner";
import {useDebounce} from "@uidotdev/usehooks";

export default function LoginPage() {
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

            router.replace('/login', {scroll: false});
        }
    }, [searchParams, router]);

    return (
        <div className="bg-muted flex min-h-svh flex-col items-center justify-center p-6 md:p-10">
            <div className="w-full max-w-sm md:max-w-3xl">
                <LoginForm/>
            </div>
        </div>
    );
}
