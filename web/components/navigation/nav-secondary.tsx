"use client"

import * as React from "react"
import {usePathname} from "next/navigation";
import {type Icon} from "@tabler/icons-react"
import {
    SidebarGroup,
    SidebarGroupContent,
    SidebarGroupLabel,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
} from "@/components/ui/sidebar"
import Link from "next/link";

export function NavSecondary({
                                 items,
                                 label,
                                 ...props
                             }: {
    items: {
        title: string
        url: string
        icon: Icon
    }[];
    label?: string;
} & React.ComponentPropsWithoutRef<typeof SidebarGroup>) {
    const pathname = usePathname();

    return (
        <SidebarGroup {...props}>
            {label && <SidebarGroupLabel>{label}</SidebarGroupLabel>}
            <SidebarGroupContent>
                <SidebarMenu>
                    {items.map((item) => {
                        const isActive = pathname.startsWith(item.url);
                        const isExternal = item.url === "#";
                        return (
                            <SidebarMenuItem key={item.title}>
                                <SidebarMenuButton asChild isActive={isActive}>
                                    {isExternal ? (
                                        <a href={item.url}>
                                            <item.icon/>
                                            <span>{item.title}</span>
                                        </a>
                                    ) : (
                                        <Link href={item.url}>
                                            <item.icon/>
                                            <span>{item.title}</span>
                                        </Link>
                                    )}
                                </SidebarMenuButton>
                            </SidebarMenuItem>
                        )
                    })}
                </SidebarMenu>
            </SidebarGroupContent>
        </SidebarGroup>
    )
}
