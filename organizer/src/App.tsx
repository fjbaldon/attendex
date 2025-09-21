import {BrowserRouter as Router, Route, Routes, Navigate} from 'react-router-dom';
import {Toaster} from "@/components/ui/sonner";
import {AuthProvider, useAuth} from './contexts/AuthContext';
import {QueryClient, QueryClientProvider} from '@tanstack/react-query';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import AppLayout from './components/layout/AppLayout';
import EventsPage from './pages/EventsPage';
import AttendeesPage from './pages/AttendeesPage';
import ScannersPage from './pages/ScannersPage';
import EventDetailsPage from './pages/EventDetailsPage';
import DashboardPage from './pages/DashboardPage';

const queryClient = new QueryClient();

function AppRoutes() {
    const {token} = useAuth();

    return (
        <Routes>
            <Route path="/login" element={!token ? <LoginPage/> : <Navigate to="/"/>}/>
            <Route path="/register" element={!token ? <RegisterPage/> : <Navigate to="/"/>}/>
            <Route element={<AppLayout/>}>
                <Route path="/" element={token ? <DashboardPage/> : <Navigate to="/login"/>}/>
                <Route path="/events" element={token ? <EventsPage/> : <Navigate to="/login"/>}/>
                <Route path="/events/:id" element={token ? <EventDetailsPage/> : <Navigate to="/login"/>}/>
                <Route path="/attendees" element={token ? <AttendeesPage/> : <Navigate to="/login"/>}/>
                <Route path="/scanners" element={token ? <ScannersPage/> : <Navigate to="/login"/>}/>
            </Route>
            <Route path="*" element={<Navigate to={token ? "/" : "/login"}/>}/>
        </Routes>
    );
}

function App() {
    return (
        <QueryClientProvider client={queryClient}>
            <AuthProvider>
                <Router>
                    <AppRoutes/>
                </Router>
                <Toaster/>
            </AuthProvider>
        </QueryClientProvider>
    );
}

export default App;
