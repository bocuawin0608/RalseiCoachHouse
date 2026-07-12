import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../features/auth';

const ROUTE_PRIORITY = ["ADMIN", "MANAGER", "TICKET_STAFF", "TRIP_STAFF"];

const ROUTE_MAP = {
    ADMIN: "/management/dashboard",
    MANAGER: "/management/dashboard",
    TICKET_STAFF: "/staff/trips/info",
    TRIP_STAFF: "/staff",
}

export default function GuestGuard() {
    const { token, user } = useAuth();

    if (token && user) {
        const roles = user.roles || [];
        
        const highestRole = ROUTE_PRIORITY.find(role => roles.includes(role));

        if(highestRole) return <Navigate to={ROUTE_MAP[highestRole]} replace />;
    }

    return <Outlet />;
}