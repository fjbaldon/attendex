import Link from "next/link";
import {Home, Ticket, Calendar, ScanLine, Users} from "lucide-react";
import {UserNav} from "@/components/user-nav";

export function Sidebar() {
    return (
        <div className="hidden border-r bg-muted/40 md:block sticky top-0 h-screen">
            <div className="flex h-full max-h-screen flex-col">
                <div className="flex h-14 items-center border-b px-4 lg:h-[60px] lg:px-6">
                    <Link href="/" className="flex items-center gap-2 font-semibold">
                        <Ticket className="h-6 w-6"/>
                        <span>AttendEx</span>
                    </Link>
                </div>
                <div className="flex-1 overflow-auto py-4">
                    <nav className="grid items-start px-2 text-sm font-medium lg:px-4">
                        <Link
                            href="/dashboard"
                            className="flex items-center gap-3 rounded-lg px-3 py-2 text-muted-foreground transition-all hover:text-primary"
                        >
                            <Home className="h-4 w-4"/>
                            Dashboard
                        </Link>
                        <Link
                            href="/events"
                            className="flex items-center gap-3 rounded-lg px-3 py-2 text-muted-foreground transition-all hover:text-primary"
                        >
                            <Calendar className="h-4 w-4"/>
                            Events
                        </Link>
                        <Link
                            href="/scanners"
                            className="flex items-center gap-3 rounded-lg px-3 py-2 text-muted-foreground transition-all hover:text-primary"
                        >
                            <ScanLine className="h-4 w-4"/>
                            Scanners
                        </Link>
                        <Link
                            href="/attendees"
                            className="flex items-center gap-3 rounded-lg px-3 py-2 text-muted-foreground transition-all hover:text-primary"
                        >
                            <Users className="h-4 w-4"/>
                            Attendees
                        </Link>
                    </nav>
                </div>
                <div className="border-t p-2">
                    <UserNav/>
                </div>
            </div>
        </div>
    );
}
