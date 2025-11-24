"use client";

import * as React from "react";
import {z} from "zod";
import {organizationSettingsSchema} from "@/lib/schemas";
import {useOrganization} from "@/hooks/use-organization";
import {OrganizationSettingsForm} from "./organization-settings-form";
import {Skeleton} from "@/components/ui/skeleton";
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {Button} from "@/components/ui/button";

export default function OrganizationSettingsPage() {
    const {organization, isLoading, updateOrganization, isUpdating} = useOrganization();

    const handleSubmit = (values: z.infer<typeof organizationSettingsSchema>) => {
        const payload = {
            ...values,
            // Convert empty string back to null for the API if the user cleared it
            identityFormatRegex: values.identityFormatRegex || null,
        };
        updateOrganization(payload);
    };

    if (isLoading) {
        return (
            <div className="max-w-3xl space-y-6">
                <Card>
                    <CardHeader>
                        <Skeleton className="h-6 w-48 mb-2"/>
                        <Skeleton className="h-4 w-96"/>
                    </CardHeader>
                    <CardContent className="space-y-6">
                        <div className="space-y-2">
                            <Skeleton className="h-4 w-32"/>
                            <Skeleton className="h-10 w-full"/>
                        </div>
                        <div className="space-y-2">
                            <Skeleton className="h-4 w-48"/>
                            <Skeleton className="h-10 w-full"/>
                        </div>
                    </CardContent>
                </Card>
            </div>
        );
    }

    if (!organization) {
        return (
            <div className="p-4 border border-dashed rounded-lg text-muted-foreground text-center">
                Could not load organization data. Please try refreshing.
            </div>
        );
    }

    return (
        <div className="max-w-3xl space-y-8 pb-10">
            {/* Main Settings Form */}
            <OrganizationSettingsForm
                organization={organization}
                onSubmit={handleSubmit}
                isLoading={isUpdating}
            />

            {/* Danger Zone */}
            <Card className="border-red-200 dark:border-red-900/50 shadow-sm">
                <CardHeader>
                    <CardTitle className="text-red-600 dark:text-red-500 text-lg">Danger Zone</CardTitle>
                    <CardDescription>
                        Irreversible actions regarding your organization.
                    </CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                    <div className="flex flex-col sm:flex-row sm:items-center justify-between p-4 border rounded-lg bg-red-50/50 dark:bg-red-900/10 border-red-100 dark:border-red-900/20 gap-4">
                        <div className="space-y-1">
                            <p className="font-medium text-sm text-foreground">Delete Organization</p>
                            <p className="text-xs text-muted-foreground">
                                Permanently remove your organization, all events, and attendee data. This action cannot be undone.
                            </p>
                        </div>
                        <Button variant="destructive" size="sm" disabled>
                            Contact Support
                        </Button>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
}
