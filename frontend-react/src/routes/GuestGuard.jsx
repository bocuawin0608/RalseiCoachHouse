import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../features/auth';

export default function GuestGuard() {
    const { token, user } = useAuth();

    if (token && user) {
        return <Navigate to='/' replace />
    }

    return <Outlet />;
}