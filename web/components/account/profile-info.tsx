"use client";

import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {Label} from "@/components/ui/label";
import {Input} from "@/components/ui/input";
import {useAuthStore} from "@/store/auth";
import {Avatar, AvatarFallback, AvatarImage} from "@/components/ui/avatar";

export function ProfileInfo() {
    const userEmail = useAuthStore((state) => state.userEmail);
    const userInitial = userEmail ? userEmail.charAt(0).toUpperCase() : "U";

    return (
        <Card className="h-full flex flex-col">
            <CardHeader>
                <CardTitle>Profile Information</CardTitle>
                <CardDescription>
                    Your account details. Contact an administrator to update your email.
                </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6 flex-1">
                <div className="flex items-center gap-4">
                    <Avatar className="h-16 w-16 border-2 border-muted">
                        <AvatarImage src="" />
                        <AvatarFallback className="text-xl bg-primary/10 text-primary">
                            {userInitial}
                        </AvatarFallback>
                    </Avatar>
                    <div className="space-y-1">
                        <h3 className="font-medium leading-none">Account</h3>
                        <p className="text-sm text-muted-foreground">
                            {userEmail}
                        </p>
                    </div>
                </div>

                <div className="space-y-2">
                    <Label>Email Address</Label>
                    <Input value={userEmail || ""} disabled className="bg-muted/50" />
                    <p className="text-[0.8rem] text-muted-foreground">
                        This is your unique identifier for logging in.
                    </p>
                </div>
            </CardContent>
        </Card>
    );
}
