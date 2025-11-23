import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {RecentActivity} from "@/types";
import {Skeleton} from "@/components/ui/skeleton";
import {formatDistanceToNow} from "date-fns";
import {Avatar, AvatarFallback} from "@/components/ui/avatar";

interface RecentActivityListProps {
    activities?: RecentActivity[];
    isLoading: boolean;
}

export function RecentActivityList({activities, isLoading}: RecentActivityListProps) {
    return (
        <Card className="h-full flex flex-col">
            <CardHeader>
                <CardTitle>Live Feed</CardTitle>
                <CardDescription>Real-time entries across all events.</CardDescription>
            </CardHeader>
            <CardContent className="flex-1 overflow-hidden">
                {isLoading ? (
                    <div className="space-y-4">
                        {[...Array(5)].map((_, i) => (
                            <div key={i} className="flex items-center gap-3">
                                <Skeleton className="h-9 w-9 rounded-full"/>
                                <div className="space-y-1 flex-1">
                                    <Skeleton className="h-3 w-24"/>
                                    <Skeleton className="h-2 w-16"/>
                                </div>
                            </div>
                        ))}
                    </div>
                ) : !activities || activities.length === 0 ? (
                    <div className="flex flex-col items-center justify-center h-full text-center text-sm text-muted-foreground min-h-[150px]">
                        No recent activity.
                    </div>
                ) : (
                    <div className="space-y-6">
                        {activities.map((item, i) => (
                            <div key={i} className="flex items-start gap-3">
                                <Avatar className="h-8 w-8 mt-0.5">
                                    <AvatarFallback className="text-xs bg-primary/10 text-primary">
                                        {item.attendeeName.charAt(0)}
                                    </AvatarFallback>
                                </Avatar>
                                <div className="flex flex-col gap-0.5 text-sm">
                                    <p className="font-medium leading-none">
                                        {item.attendeeName}
                                    </p>
                                    <p className="text-xs text-muted-foreground">
                                        Scanned at <span className="text-foreground font-medium">{item.eventName}</span>
                                    </p>
                                </div>
                                <div className="ml-auto text-xs text-muted-foreground whitespace-nowrap">
                                    {formatDistanceToNow(new Date(item.scanTime), {addSuffix: true})}
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </CardContent>
        </Card>
    );
}
