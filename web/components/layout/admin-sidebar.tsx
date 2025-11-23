"use client";

import * as React from "react";
import {IconBuilding, IconDashboard, IconUserShield} from "@tabler/icons-react";
import {NavMain} from "@/components/navigation/nav-main";
import {
    Sidebar,
    SidebarContent,
    SidebarFooter,
    SidebarHeader,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
} from "@/components/ui/sidebar";
import {Ticket} from "lucide-react";
import {useAuthStore} from "@/store/auth";
import {NavUser} from "@/components/navigation/nav-user";

const navAdmin = [
    {title: "Dashboard", url: "/admin/dashboard", icon: IconDashboard},
    {title: "Organizations", url: "/admin/organizations", icon: IconBuilding},
    {title: "Stewards", url: "/admin/stewards", icon: IconUserShield},
];

export function AdminSidebar({...props}: React.ComponentProps<typeof Sidebar>) {
    const userEmail = useAuthStore((state) => state.userEmail);

    const user = {
        name: "Admin",
        email: userEmail || "admin@example.com",
        avatar: "",
    };

    return (
        <Sidebar collapsible="offcanvas" {...props}>
            <SidebarHeader>
                <SidebarMenu>
                    <SidebarMenuItem>
                        <SidebarMenuButton
                            asChild
                            className="data-[slot=sidebar-menu-button]:!p-1.5"
                        >
                            <a href="/admin/dashboard">
                                <Ticket className="!size-5"/>
                                <span className="text-base font-semibold">AttendEx</span>
                            </a>
                        </SidebarMenuButton>
                    </SidebarMenuItem>
                </SidebarMenu>
            </SidebarHeader>
            <SidebarContent>
                <NavMain items={navAdmin}/>
            </SidebarContent>
            <SidebarFooter>
                <NavUser user={user}/>
            </SidebarFooter>
        </Sidebar>
    );
}
