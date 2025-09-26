import {IconTrendingDown, IconTrendingUp} from "@tabler/icons-react";
import {Badge} from "@/components/ui/badge";
import {
    Card,
    CardAction,
    CardDescription,
    CardFooter,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";

export function SectionCards() {
    return (
        <div
            className="*:data-[slot=card]:from-primary/5 *:data-[slot=card]:to-card dark:*:data-[slot=card]:bg-card grid grid-cols-1 gap-4 px-4 *:data-[slot=card]:bg-gradient-to-t *:data-[slot=card]:shadow-xs lg:px-6 @xl/main:grid-cols-2 @5xl/main:grid-cols-4">
            <Card className="@container/card">
                <CardHeader>
                    <CardDescription>Total Events</CardDescription>
                    <CardTitle className="text-2xl font-semibold tabular-nums @[250px]/card:text-3xl">
                        12
                    </CardTitle>
                    <CardAction>
                        <Badge variant="outline">
                            <IconTrendingUp/>
                            +2
                        </Badge>
                    </CardAction>
                </CardHeader>
                <CardFooter className="flex-col items-start gap-1.5 text-sm">
                    <div className="line-clamp-1 flex gap-2 font-medium">
                        Trending up this month <IconTrendingUp className="size-4"/>
                    </div>
                    <div className="text-muted-foreground">
                        Two new events scheduled
                    </div>
                </CardFooter>
            </Card>
            <Card className="@container/card">
                <CardHeader>
                    <CardDescription>Total Attendees</CardDescription>
                    <CardTitle className="text-2xl font-semibold tabular-nums @[250px]/card:text-3xl">
                        +2,350
                    </CardTitle>
                    <CardAction>
                        <Badge variant="outline">
                            <IconTrendingUp/>
                            +180.1%
                        </Badge>
                    </CardAction>
                </CardHeader>
                <CardFooter className="flex-col items-start gap-1.5 text-sm">
                    <div className="line-clamp-1 flex gap-2 font-medium">
                        Up 180.1% this period <IconTrendingUp className="size-4"/>
                    </div>
                    <div className="text-muted-foreground">
                        Conference registration is open
                    </div>
                </CardFooter>
            </Card>
            <Card className="@container/card">
                <CardHeader>
                    <CardDescription>Active Scanners</CardDescription>
                    <CardTitle className="text-2xl font-semibold tabular-nums @[250px]/card:text-3xl">
                        +12
                    </CardTitle>
                    <CardAction>
                        <Badge variant="outline">
                            <IconTrendingUp/>
                            +4
                        </Badge>
                    </CardAction>
                </CardHeader>
                <CardFooter className="flex-col items-start gap-1.5 text-sm">
                    <div className="line-clamp-1 flex gap-2 font-medium">
                        More scanners online <IconTrendingUp className="size-4"/>
                    </div>
                    <div className="text-muted-foreground">4 new scanners added</div>
                </CardFooter>
            </Card>
            <Card className="@container/card">
                <CardHeader>
                    <CardDescription>Live Check-ins</CardDescription>
                    <CardTitle className="text-2xl font-semibold tabular-nums @[250px]/card:text-3xl">
                        +573
                    </CardTitle>
                    <CardAction>
                        <Badge variant="outline">
                            <IconTrendingDown/>
                            -12.5%
                        </Badge>
                    </CardAction>
                </CardHeader>
                <CardFooter className="flex-col items-start gap-1.5 text-sm">
                    <div className="line-clamp-1 flex gap-2 font-medium">
                        Slower than last hour <IconTrendingDown className="size-4"/>
                    </div>
                    <div className="text-muted-foreground">
                        Waiting for next session to start
                    </div>
                </CardFooter>
            </Card>
        </div>
    );
}
