import {Header} from "@/components/header";
import {Sidebar} from "@/components/sidebar";
import {AuthGuard} from "@/components/auth/auth-guard";

export default function DashboardLayout({
                                            children,
                                        }: {
    children: React.ReactNode;
}) {
    return (
        <AuthGuard>
            <div className="grid min-h-screen w-full md:grid-cols-[220px_1fr] lg:grid-cols-[280px_1fr]">
                <Sidebar/>
                <div className="flex flex-col">
                    <Header/>
                    <main className="flex flex-1 flex-col gap-4 p-4 sm:px-8 sm:py-6">
                        {children}
                    </main>
                </div>
            </div>
        </AuthGuard>
    );
}
