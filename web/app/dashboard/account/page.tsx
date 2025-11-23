"use client";

import * as React from "react";
import {AppSidebar} from "@/components/layout/app-sidebar";
import {SiteHeader} from "@/components/layout/site-header";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {AccountPasswordForm} from "@/components/account/account-password-form";
import {ProfileInfo} from "@/components/account/profile-info";

export default function AccountPage() {
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
                <SiteHeader title="Account"/>
                <main className="flex flex-1 flex-col p-4 lg:p-6">
                    <div className="w-full max-w-5xl mx-auto space-y-8">
                        <div>
                            <h1 className="text-2xl font-bold tracking-tight">Account Settings</h1>
                            <p className="text-muted-foreground">
                                Manage your profile and security preferences.
                            </p>
                        </div>

                        <div className="grid gap-6 md:grid-cols-2">
                            <ProfileInfo />
                            <AccountPasswordForm />
                        </div>
                    </div>
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}
