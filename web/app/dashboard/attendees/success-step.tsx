"use client";

import {IconCircleCheck} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";

interface SuccessStepProps {
    count: number;
    onClose: () => void;
}

export function SuccessStep({count, onClose}: SuccessStepProps) {
    return (
        <div className="flex flex-col items-center justify-center h-full text-center space-y-6 animate-in fade-in zoom-in-95 duration-300 min-h-[400px]">
            <div className="rounded-full bg-green-100 p-6 dark:bg-green-900/20">
                <IconCircleCheck className="h-16 w-16 text-green-600 dark:text-green-500"/>
            </div>
            <div className="space-y-2 max-w-md mx-auto px-4">
                <h3 className="text-2xl font-bold tracking-tight">Import Successful!</h3>
                <p className="text-muted-foreground text-lg">
                    <span className="font-semibold text-foreground">{count}</span> {count === 1 ? 'attendee has' : 'attendees have'} been successfully added to your organization.
                </p>
            </div>
            <Button onClick={onClose} size="lg" className="min-w-[120px] mt-4">
                Close
            </Button>
        </div>
    );
}
