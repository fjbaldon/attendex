import {AppSidebar} from "@/components/app-sidebar";
import {SiteHeader} from "@/components/site-header";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";

export default function AttendeesPage() {
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
                <SiteHeader title="Attendees"/>
                <div className="flex flex-1 flex-col p-4 lg:p-6">
                    <p className="text-muted-foreground">Manage your attendees here.</p>
                </div>
            </SidebarInset>
        </SidebarProvider>
    );
}
