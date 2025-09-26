"use client";

import {useAuthStore} from "@/store/auth";
import {useRouter} from "next/navigation";
import {useEffect, useState} from "react";

export default function AuthLayout({
                                       children,
                                   }: {
    children: React.ReactNode;
}) {
    const {isAuthenticated} = useAuthStore();
    const router = useRouter();
    const [isClient, setIsClient] = useState(false);

    useEffect(() => {
        setIsClient(true);
    }, []);

    useEffect(() => {
        if (isClient && isAuthenticated) {
            router.replace("/dashboard");
        }
    }, [isClient, isAuthenticated, router]);

    if (!isClient || isAuthenticated) {
        return null;
    }

    return <>{children}</>;
}
