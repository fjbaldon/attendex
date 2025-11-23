import {Card, CardContent, CardFooter, CardHeader, CardTitle} from "@/components/ui/card";
import {DashboardStats} from "@/types";
import {Skeleton} from "@/components/ui/skeleton";

interface SectionCardsProps {
    stats?: DashboardStats;
    isLoading: boolean;
}

export function SectionCards({stats, isLoading}: SectionCardsProps) {
    const cardData = [
        {title: "Total Events", value: stats?.totalEvents, description: "All scheduled events"},
        {title: "Total Attendees", value: stats?.totalAttendees, description: "Unique attendees registered"},
        {title: "Total Scanners", value: stats?.totalScanners, description: "Active scanner accounts"},
        {title: "Live Entries (1hr)", value: stats?.liveEntries, description: "Recent attendance activity"},
    ];

    return (
        <div
            className="grid grid-cols-1 gap-4 @xl/main:grid-cols-2 @5xl/main:grid-cols-4">
            {cardData.map((card, index) => (
                <Card key={index}>
                    <CardHeader className="pb-2">
                        <CardTitle className="text-sm font-medium">
                            {card.title}
                        </CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="text-3xl font-bold">
                            {isLoading ? (
                                <Skeleton className="h-8 w-20 mt-1"/>
                            ) : (
                                card.value?.toLocaleString() ?? 0
                            )}
                        </div>
                    </CardContent>
                    <CardFooter className="pt-0">
                        <p className="text-xs text-muted-foreground">
                            {card.description}
                        </p>
                    </CardFooter>
                </Card>
            ))}
        </div>
    );
}
