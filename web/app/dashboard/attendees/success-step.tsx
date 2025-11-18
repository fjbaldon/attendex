"use client";

import {IconCircleCheck} from "@tabler/icons-react";
import {Button} from "@/components/ui/button";

interface SuccessStepProps {
    count: number;
    onClose: () => void;
}

export function SuccessStep({count, onClose}: SuccessStepProps) {
    return (
        <div className="flex flex-col items-center justify-center space-y-4 text-center py-12">
            <IconCircleCheck className="h-16 w-16 text-green-500"/>
            <div className="space-y-1">
                <h3 className="text-2xl font-bold">Import Successful!</h3>
                <p className="text-muted-foreground">
                    {count} {count === 1 ? 'attendee has' : 'attendees have'} been successfully added.
                </p>
            </div>
            <Button onClick={onClose}>Close</Button>
        </div>
    );
}
