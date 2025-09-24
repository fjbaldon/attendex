import {Badge} from "@/components/ui/badge";
import {Button} from "@/components/ui/button";
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {Activity, Calendar, ScanLine, Users} from "lucide-react";

export default function DashboardPage() {
    return (
        <div className="flex w-full flex-col">
            <div className="flex flex-col gap-4 sm:gap-8">
                <header>
                    <h1 className="text-2xl font-semibold leading-none tracking-tight">
                        Dashboard
                    </h1>
                </header>
                <Tabs defaultValue="overview">
                    <div className="flex items-center">
                        <TabsList>
                            <TabsTrigger value="overview">Overview</TabsTrigger>
                            <TabsTrigger value="analytics" disabled>
                                Analytics
                            </TabsTrigger>
                            <TabsTrigger value="reports" disabled>
                                Reports
                            </TabsTrigger>
                        </TabsList>
                        <div className="ml-auto flex items-center gap-2">
                            <Button size="sm" variant="outline">
                                Download
                            </Button>
                        </div>
                    </div>
                    <TabsContent value="overview" className="space-y-4">
                        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                            <Card>
                                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                    <CardTitle className="text-sm font-medium">
                                        Total Events
                                    </CardTitle>
                                    <Calendar className="h-4 w-4 text-muted-foreground"/>
                                </CardHeader>
                                <CardContent>
                                    <div className="text-2xl font-bold">12</div>
                                    <p className="text-xs text-muted-foreground">
                                        +2 since last month
                                    </p>
                                </CardContent>
                            </Card>
                            <Card>
                                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                    <CardTitle className="text-sm font-medium">
                                        Total Attendees
                                    </CardTitle>
                                    <Users className="h-4 w-4 text-muted-foreground"/>
                                </CardHeader>
                                <CardContent>
                                    <div className="text-2xl font-bold">+2350</div>
                                    <p className="text-xs text-muted-foreground">
                                        +180.1% from last month
                                    </p>
                                </CardContent>
                            </Card>
                            <Card>
                                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                    <CardTitle className="text-sm font-medium">
                                        Active Scanners
                                    </CardTitle>
                                    <ScanLine className="h-4 w-4 text-muted-foreground"/>
                                </CardHeader>
                                <CardContent>
                                    <div className="text-2xl font-bold">+12</div>
                                    <p className="text-xs text-muted-foreground">
                                        +4 since last hour
                                    </p>
                                </CardContent>
                            </Card>
                            <Card>
                                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                    <CardTitle className="text-sm font-medium">
                                        Live Check-ins
                                    </CardTitle>
                                    <Activity className="h-4 w-4 text-muted-foreground"/>
                                </CardHeader>
                                <CardContent>
                                    <div className="text-2xl font-bold">+573</div>
                                    <p className="text-xs text-muted-foreground">
                                        +201 since last hour
                                    </p>
                                </CardContent>
                            </Card>
                        </div>
                        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
                            <Card className="col-span-4">
                                <CardHeader>
                                    <CardTitle>Overview</CardTitle>
                                </CardHeader>
                                <CardContent className="pl-2">
                                    <div className="h-[350px] flex items-center justify-center text-muted-foreground">
                                        Graph will be implemented here
                                    </div>
                                </CardContent>
                            </Card>
                            <Card className="col-span-3">
                                <CardHeader>
                                    <CardTitle>Recent Check-ins</CardTitle>
                                    <CardDescription>
                                        The last 5 attendees that checked in.
                                    </CardDescription>
                                </CardHeader>
                                <CardContent>
                                    <Table>
                                        <TableHeader>
                                            <TableRow>
                                                <TableHead>Attendee</TableHead>
                                                <TableHead>Event</TableHead>
                                                <TableHead>Status</TableHead>
                                                <TableHead className="text-right">Time</TableHead>
                                            </TableRow>
                                        </TableHeader>
                                        <TableBody>
                                            <TableRow>
                                                <TableCell>
                                                    <div className="font-medium">John Doe</div>
                                                    <div className="hidden text-sm text-muted-foreground md:inline">
                                                        john.doe@email.com
                                                    </div>
                                                </TableCell>
                                                <TableCell>Tech Conference 2024</TableCell>
                                                <TableCell>
                                                    <Badge variant="outline">Checked In</Badge>
                                                </TableCell>
                                                <TableCell className="text-right">2 min ago</TableCell>
                                            </TableRow>
                                            <TableRow>
                                                <TableCell>
                                                    <div className="font-medium">Jane Smith</div>
                                                    <div className="hidden text-sm text-muted-foreground md:inline">
                                                        jane.smith@email.com
                                                    </div>
                                                </TableCell>
                                                <TableCell>Tech Conference 2024</TableCell>
                                                <TableCell>
                                                    <Badge variant="outline">Checked In</Badge>
                                                </TableCell>
                                                <TableCell className="text-right">5 min ago</TableCell>
                                            </TableRow>
                                            <TableRow>
                                                <TableCell>
                                                    <div className="font-medium">Peter Jones</div>
                                                    <div className="hidden text-sm text-muted-foreground md:inline">
                                                        peter.jones@email.com
                                                    </div>
                                                </TableCell>
                                                <TableCell>Art Exhibit</TableCell>
                                                <TableCell>
                                                    <Badge variant="outline">Checked In</Badge>
                                                </TableCell>
                                                <TableCell className="text-right">10 min ago</TableCell>
                                            </TableRow>
                                            <TableRow>
                                                <TableCell>
                                                    <div className="font-medium">Mary Jane</div>
                                                    <div className="hidden text-sm text-muted-foreground md:inline">
                                                        mary.jane@email.com
                                                    </div>
                                                </TableCell>
                                                <TableCell>Tech Conference 2024</TableCell>
                                                <TableCell>
                                                    <Badge variant="outline">Checked In</Badge>
                                                </TableCell>
                                                <TableCell className="text-right">12 min ago</TableCell>
                                            </TableRow>
                                            <TableRow>
                                                <TableCell>
                                                    <div className="font-medium">Sam Wilson</div>
                                                    <div className="hidden text-sm text-muted-foreground md:inline">
                                                        sam.wilson@email.com
                                                    </div>
                                                </TableCell>
                                                <TableCell>Career Fair</TableCell>
                                                <TableCell>
                                                    <Badge variant="outline">Checked In</Badge>
                                                </TableCell>
                                                <TableCell className="text-right">15 min ago</TableCell>
                                            </TableRow>
                                        </TableBody>
                                    </Table>
                                </CardContent>
                            </Card>
                        </div>
                    </TabsContent>
                </Tabs>
            </div>
        </div>
    );
}
