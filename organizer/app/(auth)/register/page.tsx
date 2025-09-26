import {Ticket} from "lucide-react";
import {RegisterForm} from "@/components/register-form";

export default function RegisterPage() {
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
                <RegisterForm/>
                <div
                    className="text-muted-foreground *:[a]:hover:text-primary text-center text-xs text-balance *:[a]:underline *:[a]:underline-offset-4">
                    By clicking continue, you agree to our{" "}
                    <a href="#">Terms of Service</a> and <a href="#">Privacy Policy</a>.
                </div>
            </div>
        </div>
    );
}
