import Image from "next/image";
import {cn} from "@/lib/utils";

interface LogoProps {
    className?: string;
}

export function Logo({ className }: LogoProps) {
    return (
        <div className={cn("relative aspect-square", className)}>
            <Image
                src="/logo.svg"
                alt="AttendEx Logo"
                fill
                className="object-contain"
                priority
            />
        </div>
    );
}
