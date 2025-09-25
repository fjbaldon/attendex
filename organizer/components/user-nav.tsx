"use client";

import {Avatar, AvatarFallback, AvatarImage} from "@/components/ui/avatar";
import {Button} from "@/components/ui/button";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuGroup,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {useAuth} from "@/hooks/use-auth";
import {
    CreditCard,
    LogOut,
    MoreVertical,
    Settings,
    User,
} from "lucide-react";
import {useAuthStore} from "@/store/auth";

export function UserNav() {
    const {logout} = useAuth();
    const userEmail = useAuthStore((state) => state.userEmail);
    const userInitial = userEmail ? userEmail.charAt(0).toUpperCase() : "U";

    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button
                    variant="ghost"
                    className="flex h-auto w-full items-center justify-start gap-2 p-2"
                >
                    <Avatar className="h-8 w-8">
                        <AvatarImage src="" alt="User avatar"/>
                        <AvatarFallback>{userInitial}</AvatarFallback>
                    </Avatar>
                    <div className="flex flex-col items-start truncate">
                        <p className="text-sm font-medium leading-none">Organizer</p>
                        <p className="text-xs leading-none text-muted-foreground truncate">
                            {userEmail}
                        </p>
                    </div>
                    <MoreVertical className="ml-auto h-4 w-4"/>
                </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent
                className="w-56"
                align="end"
                forceMount
                side="right"
                sideOffset={4}
            >
                <DropdownMenuLabel className="font-normal">
                    <div className="flex flex-col space-y-1">
                        <p className="text-sm font-medium leading-none">Organizer</p>
                        <p className="text-xs leading-none text-muted-foreground truncate">
                            {userEmail}
                        </p>
                    </div>
                </DropdownMenuLabel>
                <DropdownMenuSeparator/>
                <DropdownMenuGroup>
                    <DropdownMenuItem disabled>
                        <User className="mr-2 h-4 w-4"/>
                        <span>Account</span>
                    </DropdownMenuItem>
                    <DropdownMenuItem disabled>
                        <Settings className="mr-2 h-4 w-4"/>
                        <span>Settings</span>
                    </DropdownMenuItem>
                    <DropdownMenuItem disabled>
                        <CreditCard className="mr-2 h-4 w-4"/>
                        <span>Billing</span>
                    </DropdownMenuItem>
                </DropdownMenuGroup>
                <DropdownMenuSeparator/>
                <DropdownMenuItem onClick={() => logout()}>
                    <LogOut className="mr-2 h-4 w-4"/>
                    <span>Log out</span>
                </DropdownMenuItem>
            </DropdownMenuContent>
        </DropdownMenu>
    );
}
