import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {Table, TableBody, TableCell, TableRow} from "@/components/ui/table";
import {Skeleton} from "@/components/ui/skeleton";
import {OrganizationSummary} from "@/types";
import Link from "next/link";
import {Button} from "@/components/ui/button";

interface AttentionRequiredProps {
    organizations?: OrganizationSummary[];
    isLoading: boolean;
}

export function AttentionRequired({organizations, isLoading}: AttentionRequiredProps) {
    return (
        <Card className="h-full flex flex-col">
            <CardHeader>
                <CardTitle>Attention Required</CardTitle>
                <CardDescription>Organizations that are inactive or suspended.</CardDescription>
            </CardHeader>
            <CardContent className="flex-grow">
                {isLoading ? (
                    <div className="space-y-4">
                        {[...Array(3)].map((_, i) => <Skeleton key={i} className="h-10 w-full"/>)}
                    </div>
                ) : !organizations || organizations.length === 0 ? (
                    <div className="flex flex-col items-center justify-center h-full text-center">
                        <p className="text-sm font-medium">No accounts require attention.</p>
                        <p className="text-sm text-muted-foreground">All organizations are active.</p>
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
                                        <TableCell className="w-2/5 text-right">
                                            <Button asChild variant="secondary" size="sm">
                                                <Link href="/admin/organizations">Review</Link>
                                            </Button>
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
