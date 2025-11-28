"use client";

import * as React from "react";
import {useState} from "react";
import {AppSidebar} from "@/components/layout/app-sidebar";
import {SiteHeader} from "@/components/layout/site-header";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {useEvents} from "@/hooks/use-events";
import {useAnalytics} from "@/hooks/use-analytics";
import {Skeleton} from "@/components/ui/skeleton";
import {IconActivity, IconCheck, IconSearch} from "@tabler/icons-react";
import {useAttributes} from "@/hooks/use-attributes";
import {Popover, PopoverContent, PopoverTrigger} from "@/components/ui/popover";
import {Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList} from "@/components/ui/command";
import {Button} from "@/components/ui/button";
import {cn} from "@/lib/utils";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {EventOverviewTab} from "@/components/analytics/event-overview-tab";
import {SessionOperationsTab} from "@/components/analytics/session-operations-tab";

export default function AnalyticsPage() {
    const [selectedEventId, setSelectedEventId] = useState<string>("");
    const [openCombobox, setOpenCombobox] = useState(false);
    const [selectedAttribute, setSelectedAttribute] = useState<string>("");

    const {eventsData, isLoadingEvents} = useEvents(0, 100);
    const events = eventsData?.content ?? [];
    const {definitions: attributes, isLoading: isLoadingAttributes} = useAttributes();
    const {breakdown, stats, isLoadingStats, isLoadingBreakdown} = useAnalytics(selectedEventId, selectedAttribute);

    const selectedEvent = events.find((e) => String(e.id) === selectedEventId);
    const sessions = selectedEvent?.sessions.sort((a, b) => new Date(a.targetTime).getTime() - new Date(b.targetTime).getTime()) || [];

    React.useEffect(() => {
        if (!selectedAttribute && attributes.length > 0) {
            setSelectedAttribute(attributes[0].name);
        }
    }, [attributes, selectedAttribute]);

    const selectedEventName = events.find((e) => String(e.id) === selectedEventId)?.name;

    return (
        <SidebarProvider style={{"--sidebar-width": "calc(var(--spacing) * 72)", "--header-height": "calc(var(--spacing) * 12)"} as React.CSSProperties}>
            <AppSidebar variant="inset"/>
            <SidebarInset>
                <SiteHeader title="Analytics & Insights"/>
                <main className="flex-1 p-4 lg:p-6 overflow-y-auto">
                    <div className="w-full max-w-6xl mx-auto space-y-6">

                        {/* 1. HEADER CONTROL BAR */}
                        <div className="flex flex-col sm:flex-row gap-4 justify-between items-start sm:items-center bg-background/50 backdrop-blur-sm sticky top-0 z-10 pb-4 border-b">
                            <div className="w-full sm:w-72">
                                {isLoadingEvents ? <Skeleton className="h-10 w-full"/> : (
                                    <Popover open={openCombobox} onOpenChange={setOpenCombobox}>
                                        <PopoverTrigger asChild>
                                            <Button
                                                variant="outline"
                                                role="combobox"
                                                aria-expanded={openCombobox}
                                                className="w-full justify-between shadow-sm"
                                            >
                                                {selectedEventId
                                                    ? selectedEventName
                                                    : "Select Event to Analyze..."}
                                                <IconSearch className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                                            </Button>
                                        </PopoverTrigger>
                                        <PopoverContent className="w-[300px] p-0">
                                            <Command>
                                                <CommandInput placeholder="Search event..." />
                                                <CommandList>
                                                    <CommandEmpty>No event found.</CommandEmpty>
                                                    <CommandGroup>
                                                        {events.map((event) => (
                                                            <CommandItem
                                                                key={event.id}
                                                                value={event.name}
                                                                onSelect={() => {
                                                                    setSelectedEventId(String(event.id));
                                                                    setOpenCombobox(false);
                                                                }}
                                                            >
                                                                <IconCheck
                                                                    className={cn(
                                                                        "mr-2 h-4 w-4",
                                                                        selectedEventId === String(event.id) ? "opacity-100" : "opacity-0"
                                                                    )}
                                                                />
                                                                {event.name}
                                                            </CommandItem>
                                                        ))}
                                                    </CommandGroup>
                                                </CommandList>
                                            </Command>
                                        </PopoverContent>
                                    </Popover>
                                )}
                            </div>
                        </div>

                        {/* 2. CONTENT AREA */}
                        <div className="space-y-6">
                            {!selectedEventId ? (
                                <div className="h-[60vh] flex flex-col items-center justify-center text-muted-foreground border-2 border-dashed rounded-xl bg-muted/5">
                                    <IconActivity className="h-16 w-16 mb-4 opacity-20"/>
                                    <p className="text-lg font-medium">No Event Selected</p>
                                    <p className="text-sm">Choose an event from the dropdown above to view insights.</p>
                                </div>
                            ) : (
                                <Tabs defaultValue="overview" className="w-full">
                                    <TabsList className="grid w-full grid-cols-2 lg:w-[400px] mb-4">
                                        <TabsTrigger value="overview">Event Overview</TabsTrigger>
                                        <TabsTrigger value="sessions">Sessions & Ops</TabsTrigger>
                                    </TabsList>

                                    <TabsContent value="overview">
                                        <EventOverviewTab
                                            eventId={selectedEventId}
                                            stats={stats}
                                            breakdown={breakdown}
                                            attributes={attributes}
                                            selectedAttribute={selectedAttribute}
                                            setSelectedAttribute={setSelectedAttribute}
                                            isLoadingStats={isLoadingStats}
                                            isLoadingAttributes={isLoadingAttributes}
                                            isLoadingBreakdown={isLoadingBreakdown}
                                        />
                                    </TabsContent>

                                    <TabsContent value="sessions">
                                        <SessionOperationsTab
                                            stats={stats}
                                            sessions={sessions} // PASS SESSIONS HERE
                                            eventId={selectedEventId} // PASS EVENT ID FOR COHORT
                                            isLoadingStats={isLoadingStats}
                                        />
                                    </TabsContent>
                                </Tabs>
                            )}
                        </div>
                    </div>
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}
