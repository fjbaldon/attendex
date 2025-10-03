"use client";
import {useAuthStore} from "@/store/auth";
import {useRouter} from "next/navigation";
import {useEffect, useState} from "react";

export function AuthGuard({children}: { children: React.ReactNode }) {
    const {isAuthenticated, forcePasswordChange} = useAuthStore();
    const router = useRouter();
    const [isClient, setIsClient] = useState(false);
    useEffect(() => {
        setIsClient(true);
    }, []);

    useEffect(() => {
        if (!isClient) return;

        if (!isAuthenticated) {
            router.replace("/login");
            return;
        }

        if (isAuthenticated && forcePasswordChange) {
            router.replace("/force-password-change");
        }
    }, [isClient, isAuthenticated, forcePasswordChange, router]);

    if (!isClient || !isAuthenticated || forcePasswordChange) {
        return null;
    }

    return <>{children}</>;
}
