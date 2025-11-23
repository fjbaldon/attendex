"use client";

import * as React from "react";
import {useState} from "react";
import {AppSidebar} from "@/components/layout/app-sidebar";
import {SiteHeader} from "@/components/layout/site-header";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Skeleton} from "@/components/ui/skeleton";
import {IconDownload, IconReportAnalytics} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";
import {useEvents} from "@/hooks/use-events";
import {toast} from "sonner";
import {useAuthStore} from "@/store/auth";

export default function ReportsPage() {
    const [selectedEventId, setSelectedEventId] = useState<string>("");
    const [isDownloading, setIsDownloading] = useState(false);

    const {eventsData, isLoadingEvents} = useEvents(0, 100);
    const events = eventsData?.content ?? [];
    const accessToken = useAuthStore((state) => state.accessToken);

    const handleDownload = async () => {
        if (!selectedEventId) {
            toast.error("Please select an event.");
            return;
        }

        setIsDownloading(true);
        toast.info("Generating PDF...", {description: "Download will start shortly."});

        try {
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/v1/reports/events/${selectedEventId}/pdf`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${accessToken}`,
                },
            });

            // FIXED: Handle error directly instead of throwing
            if (!response.ok) {
                console.error("Report generation failed with status:", response.status);
                toast.error("Download Failed", {description: "Server could not generate the PDF."});
                return; // Exit the function early
            }

            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `report-event-${selectedEventId}.pdf`;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);

            toast.success("Download Complete");
        } catch (error) {
            console.error(error);
            toast.error("Download Failed", {description: "An unexpected network error occurred."});
        } finally {
            setIsDownloading(false);
        }
    };

    return (
        <SidebarProvider style={{
            "--sidebar-width": "calc(var(--spacing) * 72)",
            "--header-height": "calc(var(--spacing) * 12)"
        } as React.CSSProperties}>
            <AppSidebar variant="inset"/>
            <SidebarInset>
                <SiteHeader title="Reports"/>
                <main className="flex-1 p-4 lg:p-6">
                    <div className="w-full max-w-xl mx-auto space-y-8 pt-10">

                        <div className="text-center space-y-2">
                            <h2 className="text-2xl font-bold">Export Attendance Data</h2>
                            <p className="text-muted-foreground">Select an event to download the official PDF
                                report.</p>
                        </div>

                        <div className="bg-card border rounded-xl p-6 shadow-sm space-y-6">
                            <div className="space-y-2">
                                <label className="text-sm font-medium">Select Event</label>
                                {isLoadingEvents ? <Skeleton className="h-10 w-full"/> : (
                                    <Select value={selectedEventId} onValueChange={setSelectedEventId}>
                                        <SelectTrigger>
                                            <SelectValue placeholder="Choose an event..."/>
                                        </SelectTrigger>
                                        <SelectContent>
                                            {events.map(event => (
                                                <SelectItem key={event.id} value={String(event.id)}>
                                                    {event.name}
                                                </SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>
                                )}
                            </div>

                            <Button
                                className="w-full"
                                size="lg"
                                onClick={handleDownload}
                                disabled={!selectedEventId || isDownloading}
                            >
                                {isDownloading ? (
                                    "Generating..."
                                ) : (
                                    <>
                                        <IconDownload className="mr-2 h-5 w-5"/>
                                        Download PDF Report
                                    </>
                                )}
                            </Button>
                        </div>

                        {!selectedEventId && (
                            <div
                                className="flex flex-col items-center justify-center text-muted-foreground opacity-50 pt-10">
                                <IconReportAnalytics className="h-24 w-24 mb-4" stroke={1}/>
                                <p>Select an event to get started</p>
                            </div>
                        )}
                    </div>
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}
