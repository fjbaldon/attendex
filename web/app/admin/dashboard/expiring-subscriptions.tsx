import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {Table, TableBody, TableCell, TableRow} from "@/components/ui/table";
import {Skeleton} from "@/components/ui/skeleton";
import {OrganizationSummary} from "@/types";
import {format, formatDistanceToNow} from "date-fns";
import Link from "next/link";

interface ExpiringSubscriptionsProps {
    organizations?: OrganizationSummary[];
    isLoading: boolean;
}

export function ExpiringSubscriptions({organizations, isLoading}: ExpiringSubscriptionsProps) {
    return (
        <Card className="h-full flex flex-col">
            <CardHeader>
                <CardTitle>Expiring Soon</CardTitle>
                <CardDescription>Organizations with subscriptions expiring in the next 30 days.</CardDescription>
            </CardHeader>
            <CardContent className="flex-grow">
                {isLoading ? (
                    <div className="space-y-4">
                        {[...Array(3)].map((_, i) => <Skeleton key={i} className="h-10 w-full"/>)}
                    </div>
                ) : !organizations || organizations.length === 0 ? (
                    <div className="flex flex-col items-center justify-center h-full text-center">
                        <p className="text-sm font-medium">No subscriptions are expiring soon.</p>
                        <p className="text-sm text-muted-foreground">All accounts are stable.</p>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <Table className="table-fixed w-full">
                            <TableBody>
                                {organizations.map((org) => (
                                    <TableRow key={org.id}>
                                        <TableCell className="w-3/5 truncate font-medium">
                                            <Link href="/admin/organizations"
                                                  className="hover:underline underline-offset-4">
                                                {org.name}
                                            </Link>
                                        </TableCell>
                                        <TableCell className="w-2/5 text-right text-muted-foreground text-sm">
                                            {formatDistanceToNow(new Date(org.date), {addSuffix: true})}
                                            <div className="text-xs">({format(new Date(org.date), "MMM d, yyyy")})</div>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </div>
                )}
            </CardContent>
        </Card>
    );
}
