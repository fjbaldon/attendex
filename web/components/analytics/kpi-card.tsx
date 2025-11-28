import React from "react";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Skeleton} from "@/components/ui/skeleton";
import {cn} from "@/lib/utils";

interface KpiCardProps {
    title: string;
    icon: React.ElementType;
    value: string | number | undefined | null;
    subtext: string;
    loading: boolean;
    color?: string;
}

export function KpiCard({title, icon: Icon, value, subtext, loading, color}: KpiCardProps) {
    return (
        <Card className="shadow-sm">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">{title}</CardTitle>
                <Icon className="h-4 w-4 text-muted-foreground opacity-70"/>
            </CardHeader>
            <CardContent>
                {loading ? <Skeleton className="h-8 w-20"/> : (
                    <>
                        <div className={cn("text-2xl font-bold", color)}>{value ?? 0}</div>
                        <p className="text-xs text-muted-foreground mt-1">{subtext}</p>
                    </>
                )}
            </CardContent>
        </Card>
    );
}
