"use client";

import {usePathname} from "next/navigation";
import {type Icon} from "@tabler/icons-react";
import {
    SidebarGroup,
    SidebarGroupContent,
    SidebarGroupLabel,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
} from "@/components/ui/sidebar";

export function NavMain({
                            items,
                            label
                        }: {
    items: {
        title: string;
        url: string;
        icon?: Icon;
    }[];
    label?: string;
}) {
    const pathname = usePathname();

    return (
        <SidebarGroup>
            {label && <SidebarGroupLabel>{label}</SidebarGroupLabel>}
            <SidebarGroupContent>
                <SidebarMenu>
                    {items.map((item) => {
                        const isActive = item.url === '/dashboard'
                            ? pathname === item.url
                            : pathname.startsWith(item.url);

                        return (
                            <SidebarMenuItem key={item.title}>
                                <SidebarMenuButton asChild tooltip={item.title} isActive={isActive}>
                                    <a href={item.url}>
                                        {item.icon && <item.icon/>}
                                        <span>{item.title}</span>
                                    </a>
                                </SidebarMenuButton>
                            </SidebarMenuItem>
                        );
                    })}
                </SidebarMenu>
            </SidebarGroupContent>
        </SidebarGroup>
    );
}
