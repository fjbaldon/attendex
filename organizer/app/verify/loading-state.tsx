import {StatusCardLayout} from "@/components/shared/status-card-layout";
import {LoaderCircle} from "lucide-react";
import React from "react";

export function VerificationLoadingState() {
    return (
        <StatusCardLayout>
            <div className="mb-4">
                <LoaderCircle className="size-8 animate-spin text-muted-foreground"/>
            </div>
            <h1 className="text-2xl font-bold">Verifying...</h1>
            <p className="text-muted-foreground mt-2 text-balance">
                Please wait a moment.
            </p>
        </StatusCardLayout>
    );
}
