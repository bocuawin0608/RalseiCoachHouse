// components/guards/GuestGuard.jsx
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../features/auth';

export default function GuestGuard() {
    const { token, user } = useAuth();
    const ROUTE_PRIORITY = ["ADMIN", "MANAGER", "TICKET_STAFF", "TRIP_STAFF", "CUSTOMER"];

    const ROUTE_MAP = {
        ADMIN: "/management/dashboard",
        MANAGER: "/management/dashboard",
        TICKET_STAFF: "/staff/ticket/sell",
        TRIP_STAFF: "/staff/trip/scan",
        CUSTOMER: "/"
    }

    return <Outlet />;
}