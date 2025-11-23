import {Card, CardContent, CardFooter, CardHeader, CardTitle} from "@/components/ui/card";
import {Skeleton} from "@/components/ui/skeleton";
import {AdminDashboardStats} from "@/types";

interface AdminStatsCardsProps {
    stats?: AdminDashboardStats;
    isLoading: boolean;
}

export function AdminStatsCards({stats, isLoading}: AdminStatsCardsProps) {
    const cardData = [
        {
            title: "Total Organizations",
            value: stats?.totalOrganizations,
            description: "All organizations, regardless of status."
        },
        {
            title: "Active Organizations",
            value: stats?.activeOrganizations,
            description: "Organizations with an active subscription."
        },
        {
            title: "Trial Subscriptions",
            value: stats?.trialSubscriptions,
            description: "Accounts currently in a trial period."
        },
        {
            title: "Suspended Accounts",
            value: stats?.suspendedAccounts,
            description: "Organizations manually suspended by an admin."
        },
    ];

    return (
        <div className="grid grid-cols-1 gap-4 @xl/main:grid-cols-2 @5xl/main:grid-cols-4">
            {cardData.map((card, index) => (
                <Card key={index}>
                    <CardHeader className="pb-2">
                        <CardTitle className="text-sm font-medium">{card.title}</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="text-3xl font-bold">
                            {isLoading ? (
                                <Skeleton className="h-8 w-16 mt-1"/>
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
