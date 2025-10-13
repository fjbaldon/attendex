"use client";

import * as React from "react";
import {SiteHeader} from "@/components/layout/site-header";
import {Tabs, TabsList, TabsTrigger} from "@/components/ui/tabs";
import Link from "next/link";
import {usePathname} from "next/navigation";
import {AppSidebar} from "@/components/layout/app-sidebar";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";

export default function SettingsLayout({children}: { children: React.ReactNode }) {
    const pathname = usePathname();

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
                        {/* The redundant title block has been removed from here */}
                        <Tabs value={pathname} className="w-full">
                            <TabsList className="grid w-full grid-cols-2 sm:w-96">
                                <TabsTrigger value="/dashboard/settings/organization" asChild>
                                    <Link href="/dashboard/settings/organization">Organization</Link>
                                </TabsTrigger>
                                <TabsTrigger value="/dashboard/settings/custom-fields" asChild>
                                    <Link href="/dashboard/settings/custom-fields">Custom Fields</Link>
                                </TabsTrigger>
                            </TabsList>
                            <div className="pt-6">
                                {children}
                            </div>
                        </Tabs>
                    </div>
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}
