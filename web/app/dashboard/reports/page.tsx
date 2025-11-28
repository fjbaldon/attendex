"use client";

import * as React from "react";
import {useState} from "react";
import {AppSidebar} from "@/components/layout/app-sidebar";
import {SiteHeader} from "@/components/layout/site-header";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Skeleton} from "@/components/ui/skeleton";
import {IconFileText, IconFilter, IconInfoCircle, IconX} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";
import {useEvents} from "@/hooks/use-events";
import {toast} from "sonner";
import {useAuthStore} from "@/store/auth";
import {Label} from "@/components/ui/label";
import {RadioGroup, RadioGroupItem} from "@/components/ui/radio-group";
import {useAttributes} from "@/hooks/use-attributes";
import {Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle} from "@/components/ui/card";

export default function ReportsPage() {
    const [selectedEventId, setSelectedEventId] = useState<string>("");
    const [selectedSessionId, setSelectedSessionId] = useState<string>("all");

    const [reportType, setReportType] = useState<string>("All");
    const [filters, setFilters] = useState<{ attribute: string; value: string }[]>([]);
    const [isDownloading, setIsDownloading] = useState(false);

    const {eventsData, isLoadingEvents} = useEvents(0, 100);

    const events = React.useMemo(() => eventsData?.content ?? [], [eventsData]);

    const {definitions: attributes, isLoading: isLoadingAttributes} = useAttributes();
    const accessToken = useAuthStore((state) => state.accessToken);

    const sessions = React.useMemo(() => {
        const selectedEvent = events.find(e => String(e.id) === selectedEventId);
        return selectedEvent?.sessions || [];
    }, [events, selectedEventId]);

    // --- UX LOGIC START ---

    React.useEffect(() => {
        setSelectedSessionId("all");
        setReportType("All");
    }, [selectedEventId]);

    React.useEffect(() => {
        if (selectedSessionId !== "all") {
            const session = sessions.find(s => String(s.id) === selectedSessionId);
            if (session) {
                setReportType(session.intent);
            }
        } else {
            setReportType("All");
        }
    }, [selectedSessionId, sessions]);

    const isSessionSelected = selectedSessionId !== "all";

    // --- UX LOGIC END ---

    const addFilter = () => {
        setFilters([...filters, { attribute: "", value: "" }]);
    };

    const removeFilter = (index: number) => {
        setFilters(filters.filter((_, i) => i !== index));
    };

    const updateFilter = (index: number, field: 'attribute' | 'value', val: string) => {
        const newFilters = [...filters];
        newFilters[index][field] = val;
        if (field === 'attribute') newFilters[index].value = "";
        setFilters(newFilters);
    };

    const handleGenerateReport = async () => {
        if (!selectedEventId) {
            toast.error("Please select an event.");
            return;
        }

        const validFilters = filters.filter(f => f.attribute && f.value);
        setIsDownloading(true);
        toast.info("Processing report...");

        try {
            const params = new URLSearchParams();
            if (reportType !== 'All') params.append("type", reportType);
            if (selectedSessionId !== 'all') params.append("sessionId", selectedSessionId);

            validFilters.forEach(f => params.append(f.attribute, f.value));

            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/v1/reports/events/${selectedEventId}/pdf?${params.toString()}`, {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` },
            });

            if (response.status === 202) {
                toast.success("Large Report Detected", {
                    description: "The report is being generated in the background and will be emailed to you shortly.",
                    duration: 6000
                });
                return;
            }

            if (response.ok) {
                let filenamePrefix = events.find(e => String(e.id) === selectedEventId)?.name || "Event";
                if (selectedSessionId !== 'all') {
                    const sessionName = sessions.find(s => String(s.id) === selectedSessionId)?.activityName || "Session";
                    filenamePrefix += `_${sessionName}`;
                }

                const sanitizedName = filenamePrefix.replace(/[^a-z0-9]/gi, '_');
                const dateStr = new Date().toISOString().split('T')[0];
                const typeStr = reportType === 'All' ? 'Log' : `${reportType}s`;

                const blob = await response.blob();
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `${sanitizedName}_${typeStr}_${dateStr}.pdf`;
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);

                toast.success("Report Downloaded");
                return;
            }

            console.error("Report error:", response.status);
            toast.error("Failed to generate report");

        } catch (error) {
            console.error(error);
            toast.error("Network error occurred");
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
                <main className="flex-1 p-4 lg:p-6 overflow-y-auto">
                    <div className="w-full max-w-5xl mx-auto space-y-8">
                        <div>
                            <h1 className="text-2xl font-bold tracking-tight">Generate Reports</h1>
                            <p className="text-muted-foreground">
                                Export formal attendance logs in PDF format.
                            </p>
                        </div>

                        <div className="grid gap-6 md:grid-cols-2">

                            {/* Left Column: Configuration */}
                            <div className="flex flex-col gap-6">
                                <Card>
                                    <CardHeader>
                                        <CardTitle>Configuration</CardTitle>
                                        <CardDescription>Select the event scope and data type.</CardDescription>
                                    </CardHeader>
                                    <CardContent className="space-y-6">

                                        {/* EVENT SELECTOR */}
                                        <div className="space-y-3">
                                            <Label>Target Event</Label>
                                            {isLoadingEvents ? <Skeleton className="h-10 w-full"/> : (
                                                <Select value={selectedEventId} onValueChange={setSelectedEventId}>
                                                    <SelectTrigger className="h-11 w-full">
                                                        <SelectValue placeholder="Select an event..."/>
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

                                        {/* SESSION SELECTOR */}
                                        <div className="space-y-3">
                                            <Label>Specific Session (Optional)</Label>
                                            <Select
                                                value={selectedSessionId}
                                                onValueChange={setSelectedSessionId}
                                                disabled={!selectedEventId || sessions.length === 0}
                                            >
                                                <SelectTrigger className="h-11 w-full">
                                                    <SelectValue placeholder={!selectedEventId ? "Select an event first" : "All Sessions (Entire Event)"}/>
                                                </SelectTrigger>
                                                <SelectContent>
                                                    <SelectItem value="all" className="font-medium">All Sessions (Entire Event)</SelectItem>
                                                    {sessions.map(session => (
                                                        <SelectItem key={session.id} value={String(session.id)}>
                                                            {session.activityName} <span className="text-muted-foreground ml-1">({session.intent})</span>
                                                        </SelectItem>
                                                    ))}
                                                </SelectContent>
                                            </Select>
                                        </div>

                                        {/* RECORD TYPE SELECTOR */}
                                        <div className="space-y-3">
                                            <Label className={isSessionSelected ? "text-muted-foreground" : ""}>Record Type</Label>

                                            <RadioGroup
                                                value={reportType}
                                                onValueChange={setReportType}
                                                className="flex flex-col space-y-1"
                                                disabled={isSessionSelected}
                                            >
                                                <div className="flex items-center space-x-2">
                                                    <RadioGroupItem value="All" id="r1" />
                                                    <Label htmlFor="r1" className="font-normal cursor-pointer">All Records</Label>
                                                </div>
                                                <div className="flex items-center space-x-2">
                                                    <RadioGroupItem value="Arrival" id="r2" />
                                                    <Label htmlFor="r2" className="font-normal cursor-pointer">Arrivals Only</Label>
                                                </div>
                                                <div className="flex items-center space-x-2">
                                                    <RadioGroupItem value="Departure" id="r3" />
                                                    <Label htmlFor="r3" className="font-normal cursor-pointer">Departures Only</Label>
                                                </div>
                                            </RadioGroup>

                                            {/* FIX: Replaced Alert with Flexbox Div */}
                                            {isSessionSelected && (
                                                <div className="flex items-start gap-2 rounded-md bg-muted/50 p-2.5 text-xs text-muted-foreground mt-1">
                                                    <IconInfoCircle className="h-4 w-4 shrink-0 mt-0.5" />
                                                    <p className="leading-tight">
                                                        Record type is locked because a specific <span className="font-semibold text-foreground">{reportType}</span> session is selected.
                                                    </p>
                                                </div>
                                            )}
                                        </div>
                                    </CardContent>
                                </Card>
                            </div>

                            {/* Right Column: Filters */}
                            <div className="flex flex-col gap-6">
                                <Card>
                                    <CardHeader>
                                        <CardTitle>Filters</CardTitle>
                                        <CardDescription>Optional: Narrow down records by attribute.</CardDescription>
                                    </CardHeader>
                                    <CardContent className="space-y-4">

                                        {filters.length === 0 && (
                                            <div className="text-sm text-muted-foreground text-center py-8 border-2 border-dashed rounded-lg bg-muted/10">
                                                No filters applied.<br/>The report will include all attendees.
                                            </div>
                                        )}

                                        <div className="space-y-3">
                                            {filters.map((filter, index) => {
                                                const attrDef = attributes.find(a => a.name === filter.attribute);
                                                const options = attrDef?.options || [];

                                                return (
                                                    <div key={index} className="grid grid-cols-[1fr_1fr_auto] gap-2 items-center animate-in fade-in slide-in-from-top-1">
                                                        <Select value={filter.attribute} onValueChange={(val) => updateFilter(index, 'attribute', val)}>
                                                            <SelectTrigger className="h-9 w-full">
                                                                <SelectValue placeholder="Attribute" />
                                                            </SelectTrigger>
                                                            <SelectContent>
                                                                {attributes.map(attr => (
                                                                    <SelectItem key={attr.id} value={attr.name}>{attr.name}</SelectItem>
                                                                ))}
                                                            </SelectContent>
                                                        </Select>

                                                        <Select
                                                            value={filter.value}
                                                            onValueChange={(val) => updateFilter(index, 'value', val)}
                                                            disabled={!filter.attribute}
                                                        >
                                                            <SelectTrigger className="h-9 w-full">
                                                                <SelectValue placeholder="Value" />
                                                            </SelectTrigger>
                                                            <SelectContent>
                                                                {options.map(opt => (
                                                                    <SelectItem key={opt} value={opt}>{opt}</SelectItem>
                                                                ))}
                                                            </SelectContent>
                                                        </Select>

                                                        <Button variant="ghost" size="icon" className="h-9 w-9 text-muted-foreground hover:text-destructive" onClick={() => removeFilter(index)}>
                                                            <IconX className="h-4 w-4" />
                                                        </Button>
                                                    </div>
                                                );
                                            })}
                                        </div>

                                        <Button
                                            variant="secondary"
                                            size="sm"
                                            onClick={addFilter}
                                            disabled={isLoadingAttributes}
                                            className="w-full"
                                        >
                                            <IconFilter className="mr-2 h-4 w-4" />
                                            Add Attribute Filter
                                        </Button>
                                    </CardContent>

                                    <CardFooter className="border-t pt-6">
                                        <Button
                                            size="lg"
                                            onClick={handleGenerateReport}
                                            disabled={!selectedEventId || isDownloading}
                                            className="w-full shadow-sm"
                                        >
                                            {isDownloading ? "Processing..." : (
                                                <>
                                                    <IconFileText className="mr-2 h-5 w-5"/>
                                                    Generate Report
                                                </>
                                            )}
                                        </Button>
                                    </CardFooter>
                                </Card>
                            </div>
                        </div>
                    </div>
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}
