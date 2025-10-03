"use client";

import {
    IconCalendar,
    IconDots,
    IconEye,
    IconTrash,
    IconUsers,
} from "@tabler/icons-react";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
    SidebarGroup,
    SidebarGroupLabel,
    SidebarMenu,
    SidebarMenuAction,
    SidebarMenuButton,
    SidebarMenuItem,
    useSidebar,
} from "@/components/ui/sidebar";

interface RecentEventItem {
    id: number;
    eventName: string;
    url: string;
}

export function NavRecentEvents({items}: { items: RecentEventItem[] }) {
    const {isMobile} = useSidebar();

    return (
        <SidebarGroup>
            <SidebarGroupLabel>Recent Events</SidebarGroupLabel>
            <SidebarMenu>
                {items.map((item) => (
                    <SidebarMenuItem key={item.id}>
                        <SidebarMenuButton asChild tooltip={item.eventName}>
                            <a href={item.url}>
                                <IconCalendar/>
                                <span>{item.eventName}</span>
                            </a>
                        </SidebarMenuButton>
                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <SidebarMenuAction
                                    showOnHover
                                    className="data-[state=open]:bg-accent rounded-sm"
                                >
                                    <IconDots/>
                                    <span className="sr-only">Event Actions</span>
                                </SidebarMenuAction>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent
                                className="w-40 rounded-lg"
                                side={isMobile ? "bottom" : "right"}
                                align={isMobile ? "end" : "start"}
                            >
                                <DropdownMenuItem>
                                    <IconEye className="mr-2 h-4 w-4"/>
                                    <span>View Details</span>
                                </DropdownMenuItem>
                                <DropdownMenuItem>
                                    <IconUsers className="mr-2 h-4 w-4"/>
                                    <span>Manage Attendees</span>
                                </DropdownMenuItem>
                                <DropdownMenuSeparator/>
                                <DropdownMenuItem variant="destructive">
                                    <IconTrash className="mr-2 h-4 w-4"/>
                                    <span>Delete Event</span>
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    </SidebarMenuItem>
                ))}
            </SidebarMenu>
        </SidebarGroup>
    );
}
