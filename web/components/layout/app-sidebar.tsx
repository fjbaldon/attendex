"use client";

import * as React from "react";
import {
    IconCalendar,
    IconDashboard,
    IconGraph,
    IconHelp,
    IconReportAnalytics,
    IconScan,
    IconSettings,
    IconUsers,
    IconUserShield,
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
import Link from "next/link";

const navDashboard = [
    {title: "Dashboard", url: "/dashboard", icon: IconDashboard},
];

const navManagement = [
    {title: "Events", url: "/dashboard/events", icon: IconCalendar},
    {title: "Scanners", url: "/dashboard/scanners", icon: IconScan},
    {title: "Attendees", url: "/dashboard/attendees", icon: IconUsers},
    {title: "Organizers", url: "/dashboard/organizers", icon: IconUserShield},
];

const navData = [
    {title: "Analytics", url: "/dashboard/analytics", icon: IconGraph},
    {title: "Reports", url: "/dashboard/reports", icon: IconReportAnalytics},
];

const navSecondary = [
    {title: "Settings", url: "/dashboard/settings/organization", icon: IconSettings},
    {title: "Get Help", url: "#", icon: IconHelp},
];

export function AppSidebar({...props}: React.ComponentProps<typeof Sidebar>) {
    const userEmail = useAuthStore((state) => state.userEmail);

    const user = {
        name: "Organizer",
        email: userEmail || "organizer@example.com",
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
                            <Link href="/dashboard">
                                <Ticket className="!size-5"/>
                                <span className="text-base font-semibold">AttendEx</span>
                            </Link>
                        </SidebarMenuButton>
                    </SidebarMenuItem>
                </SidebarMenu>
            </SidebarHeader>
            <SidebarContent>
                <NavMain items={navDashboard}/>
                <NavMain items={navManagement} label="Management"/>
                <NavMain items={navData} label="Data"/>
                <NavSecondary items={navSecondary} className="mt-auto" label="General"/>
            </SidebarContent>
            <SidebarFooter>
                <NavUser user={user}/>
            </SidebarFooter>
        </Sidebar>
    );
}
