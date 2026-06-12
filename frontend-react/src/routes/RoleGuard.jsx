// components/guards/RoleGuard.jsx
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../features/auth';

export default function RoleGuard({ allowedRoles }) {
    const { token, user } = useAuth();

    if (!token) {
        const isStaffRoute = window.location.pathname.startsWith('/management') || window.location.pathname.startsWith('/staff');
        return <Navigate to={isStaffRoute ? "/staff/login" : "/login"} replace />;
    }
    
    const hasRole = user?.roles?.some(role => allowedRoles.includes(role));
    if (!hasRole) {
        return <Navigate to="/unauthorized" replace />;
    }

    return <Outlet />;
}