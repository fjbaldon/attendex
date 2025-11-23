"use client";

import * as React from "react";
import {SiteHeader} from "@/components/layout/site-header";
import {Tabs, TabsList, TabsTrigger} from "@/components/ui/tabs";
import Link from "next/link";
import {usePathname} from "next/navigation";
import {AppSidebar} from "@/components/layout/app-sidebar";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {Skeleton} from "@/components/ui/skeleton";

const settingsPages = [
    {
        path: "/dashboard/settings/organization",
        title: "Organization Details",
        description: "Update your organization's name and settings.",
    },
    {
        // FIXED: Updated path to match folder name (attributes)
        path: "/dashboard/settings/attributes",
        title: "Manage Attributes", // Updated Title to match UL
        description: "Add, edit, or remove attributes for attendees in your organization.",
    },
];

export default function SettingsLayout({children}: { children: React.ReactNode }) {
    const pathname = usePathname();
    const currentPage = settingsPages.find(p => p.path === pathname);

    const [isClient, setIsClient] = React.useState(false);

    React.useEffect(() => {
        setIsClient(true);
    }, []);

    if (!isClient) {
        return (
            <SidebarProvider>
                <div className="flex h-svh w-full">
                    <Skeleton className="hidden md:block" style={{width: 'calc(var(--spacing) * 72)'}}/>
                    <div className="flex flex-1 flex-col">
                        <Skeleton className="h-12 w-full shrink-0 border-b"/>
                        <div className="p-4 lg:p-6">
                            <Skeleton className="h-10 w-96"/>
                            <Skeleton className="mt-6 h-64 w-full"/>
                        </div>
                    </div>
                </div>
            </SidebarProvider>
        );
    }

    return (
        <SidebarProvider
            style={
                {
                    "--sidebar-width": "calc(var(--spacing) * 72)",
                    "--header-height": "calc(var(--spacing) * 12)",
                } as React.CSSProperties
            }
        >
            <AppSidebar variant="inset"/>
            <SidebarInset>
                <SiteHeader title="Settings"/>
                <main className="flex-1 p-4 lg:p-6">
                    <div className="w-full max-w-6xl mx-auto space-y-6">
                        <Tabs value={pathname} className="w-full">
                            <TabsList className="grid w-full grid-cols-2 sm:w-96">
                                <TabsTrigger value="/dashboard/settings/organization" asChild>
                                    <Link href="/dashboard/settings/organization">Organization</Link>
                                </TabsTrigger>
                                {/* FIXED: Updated value and href */}
                                <TabsTrigger value="/dashboard/settings/attributes" asChild>
                                    <Link href="/dashboard/settings/attributes">Attributes</Link>
                                </TabsTrigger>
                            </TabsList>
                        </Tabs>

                        <div className="space-y-2">
                            <h1 className="text-2xl font-semibold">{currentPage?.title}</h1>
                            <p className="text-muted-foreground text-sm">{currentPage?.description}</p>
                        </div>

                        <div className="pt-4">
                            {children}
                        </div>
                    </div>
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}
