"use client";

import {useAuthStore} from "@/store/auth";
import {useRouter} from "next/navigation";
import {useEffect} from "react";

export default function Home() {
    const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
    const router = useRouter();

    useEffect(() => {
        if (isAuthenticated) {
            router.replace("/dashboard");
        } else {
            router.replace("/login");
        }
    }, [isAuthenticated, router]);

    return null;
}
