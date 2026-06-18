import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../features/auth';

export default function RoleGuard({ allowedRoles }) {
    const { token, user } = useAuth();

    if (!token) {
        return <Navigate to="/staff/login" replace />;
    }
    
    const hasRole = user?.roles?.some(role => allowedRoles.includes(role));
    if (!hasRole) {
        return <Navigate to="/unauthorized" replace />;
    }

    return <Outlet />;
}