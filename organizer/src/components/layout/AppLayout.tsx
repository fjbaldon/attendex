import {Outlet, Link, useNavigate} from "react-router-dom";
import {Button} from "@/components/ui/button";
import {useAuth} from "@/contexts/AuthContext";
import {LayoutDashboard, Calendar, Users, ScanLine, LogOut} from 'lucide-react';

const AppLayout = () => {
    const {logout} = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <div className="flex min-h-screen">
            <aside className="w-64 bg-gray-100 p-4 flex flex-col">
                <h1 className="text-2xl font-bold mb-8">AttendEx</h1>
                <nav className="flex flex-col space-y-2">
                    <Link to="/" className="flex items-center p-2 rounded hover:bg-gray-200"><LayoutDashboard
                        className="mr-2 h-4 w-4"/> Dashboard</Link>
                    <Link to="/events" className="flex items-center p-2 rounded hover:bg-gray-200"><Calendar
                        className="mr-2 h-4 w-4"/> Events</Link>
                    <Link to="/attendees" className="flex items-center p-2 rounded hover:bg-gray-200"><Users
                        className="mr-2 h-4 w-4"/> Attendees</Link>
                    <Link to="/scanners" className="flex items-center p-2 rounded hover:bg-gray-200"><ScanLine
                        className="mr-2 h-4 w-4"/> Scanners</Link>
                </nav>
                <div className="mt-auto">
                    <Button variant="ghost" onClick={handleLogout} className="w-full justify-start">
                        <LogOut className="mr-2 h-4 w-4"/> Logout
                    </Button>
                </div>
            </aside>
            <main className="flex-1 p-8 bg-gray-50">
                <Outlet/>
            </main>
        </div>
    );
};

export default AppLayout;
