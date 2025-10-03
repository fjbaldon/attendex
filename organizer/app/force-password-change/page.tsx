import {Ticket} from "lucide-react";
import {AuthCard} from "@/components/auth/auth-card";
import {ForcePasswordChangeForm} from "@/components/shared/force-password-change-form";

export default function ForcePasswordChangePage() {
    return (
        <div className="bg-muted flex min-h-svh flex-col items-center justify-center gap-6 p-6 md:p-10">
            <div className="flex w-full max-w-sm flex-col gap-6">
                <div className="flex items-center gap-2 self-center font-semibold">
                    <div
                        className="bg-primary text-primary-foreground flex size-7 items-center justify-center rounded-md">
                        <Ticket className="size-5"/>
                    </div>
                    AttendEx
                </div>
                <AuthCard
                    title="Create a New Password"
                    description="As a security measure, you must create a new password for your account."
                >
                    <ForcePasswordChangeForm/>
                </AuthCard>
            </div>
        </div>
    );
}
