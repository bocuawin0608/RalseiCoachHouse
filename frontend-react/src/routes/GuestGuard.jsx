// components/guards/GuestGuard.jsx
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../features/auth';

export default function GuestGuard() {
    const { token, user } = useAuth();

    if (token && user) {
        const roles = user.roles || [];
        
        if (roles.includes('MANAGER') || roles.includes('ADMIN')) {
            return <Navigate to="/management/dashboard" replace />;
        }
        if (roles.includes('TICKET_STAFF')) {
            return <Navigate to="/staff/ticket/sell" replace />;
        }
        if (roles.includes('TRIP_STAFF')) {
            return <Navigate to="/staff/trip/scan" replace />;
        }
        return <Navigate to="/" replace />;
    }

    return <Outlet />;
}