"use client";

import * as React from "react";
import {
    IconChartBar,
    IconDashboard,
    IconCalendar,
    IconScan,
    IconUsers,
    IconSettings,
    IconHelp,
    IconSearch,
} from "@tabler/icons-react";
import {NavMain} from "@/components/navigation/nav-main";
import {NavSecondary} from "@/components/navigation/nav-secondary";
import {NavUser} from "@/components/navigation/nav-user";
import {
    Sidebar,
    SidebarContent,
    SidebarFooter,
    SidebarHeader,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
} from "@/components/ui/sidebar";
import {useAuthStore} from "@/store/auth";
import {Ticket} from "lucide-react";

const navMain = [
    {title: "Dashboard", url: "/dashboard", icon: IconDashboard},
    {title: "Events", url: "/dashboard/events", icon: IconCalendar},
    {title: "Scanners", url: "/dashboard/scanners", icon: IconScan},
    {title: "Attendees", url: "/dashboard/attendees", icon: IconUsers},
    {title: "Analytics", url: "#", icon: IconChartBar},
];

const navSecondary = [
    {title: "Settings", url: "#", icon: IconSettings},
    {title: "Get Help", url: "#", icon: IconHelp},
    {title: "Search", url: "#", icon: IconSearch},
];

export function AppSidebar({...props}: React.ComponentProps<typeof Sidebar>) {
    const userEmail = useAuthStore((state) => state.userEmail);

    const user = {
        name: "Organizer",
        email: userEmail || "organizer@example.com",
        avatar: "", // No avatar for now
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
                            <a href="/dashboard">
                                <Ticket className="!size-5"/>
                                <span className="text-base font-semibold">AttendEx</span>
                            </a>
                        </SidebarMenuButton>
                    </SidebarMenuItem>
                </SidebarMenu>
            </SidebarHeader>
            <SidebarContent>
                <NavMain items={navMain}/>
                <NavSecondary items={navSecondary} className="mt-auto"/>
            </SidebarContent>
            <SidebarFooter>
                <NavUser user={user}/>
            </SidebarFooter>
        </Sidebar>
    );
}
