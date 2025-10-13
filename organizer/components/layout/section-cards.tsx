import {
    Card,
    CardDescription,
    CardFooter,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
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
        {title: "Live Check-ins (1hr)", value: stats?.liveCheckIns, description: "Recent attendance activity"},
    ];

    return (
        <div
            className="grid grid-cols-1 gap-4 px-4 lg:px-6 @xl/main:grid-cols-2 @5xl/main:grid-cols-4">
            {cardData.map((card, index) => (
                <Card key={index}>
                    <CardHeader>
                        <CardDescription>{card.title}</CardDescription>
                        <CardTitle className="text-2xl font-semibold tabular-nums @[250px]/card:text-3xl">
                            {isLoading ? (
                                <Skeleton className="h-8 w-24 mt-1"/>
                            ) : (
                                card.value?.toLocaleString() ?? 0
                            )}
                        </CardTitle>
                    </CardHeader>
                    <CardFooter className="text-sm">
                        <div className="text-muted-foreground">{card.description}</div>
                    </CardFooter>
                </Card>
            ))}
        </div>
    );
}
