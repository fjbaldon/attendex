import {Card, CardContent} from "@/components/ui/card";
import Image from "next/image";
import React from "react";
import {cn} from "@/lib/utils";
import {Logo} from "@/components/shared/logo";

interface StatusCardLayoutProps {
    imageUrl?: string;
    imageAlt?: string;
    children: React.ReactNode;
}

export function StatusCardLayout({imageUrl, imageAlt, children}: StatusCardLayoutProps) {
    return (
        <div className="bg-muted flex min-h-svh flex-col items-center justify-center p-6 md:p-10">
            <Card className={cn(
                "overflow-hidden p-0 shadow-lg w-full",
                imageUrl ? "max-w-sm md:max-w-3xl" : "max-w-sm"
            )}>
                <CardContent className={cn(
                    "p-0",
                    imageUrl ? "grid md:grid-cols-2" : "block"
                )}>
                    <div className="flex flex-col items-center justify-center p-6 text-center md:p-8">
                        <div className="flex items-center gap-2 self-center font-semibold text-lg mb-6">
                            <Logo className="size-7" />
                            AttendEx
                        </div>
                        {children}
                    </div>

                    {imageUrl && imageAlt && (
                        <div className="bg-muted relative hidden md:block">
                            <Image
                                src={imageUrl}
                                alt={imageAlt}
                                fill
                                priority
                                className="absolute inset-0 h-full w-full object-cover dark:brightness-[0.3]"
                            />
                        </div>
                    )}
                </CardContent>
            </Card>
        </div>
    );
}
