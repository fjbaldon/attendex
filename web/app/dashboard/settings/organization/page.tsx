"use client";

import * as React from "react";
import {z} from "zod";
import {organizationSettingsSchema} from "@/lib/schemas";
import {useOrganization} from "@/hooks/use-organization";
import {OrganizationSettingsForm} from "./organization-settings-form";
import {Skeleton} from "@/components/ui/skeleton";

export default function OrganizationSettingsPage() {
    const {organization, isLoading, updateOrganization, isUpdating} = useOrganization();

    const handleSubmit = (values: z.infer<typeof organizationSettingsSchema>) => {
        const payload = {
            ...values,
            identityFormatRegex: values.identityFormatRegex || null,
        };
        updateOrganization(payload);
    };

    return (
        <div className="max-w-2xl">
            {isLoading ? (
                <div className="space-y-6">
                    <Skeleton className="h-10 w-full"/>
                    <Skeleton className="h-10 w-full"/>
                    <div className="flex justify-end">
                        <Skeleton className="h-10 w-24"/>
                    </div>
                </div>
            ) : organization ? (
                <OrganizationSettingsForm
                    organization={organization}
                    onSubmit={handleSubmit}
                    isLoading={isUpdating}
                />
            ) : (
                <p className="text-muted-foreground">Could not load organization data.</p>
            )}
        </div>
    );
}
