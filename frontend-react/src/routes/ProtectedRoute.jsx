import { Navigate, Outlet } from 'react-router-dom';

export default function ProtectedRoute({ isAuthenticated, userRole, allowedRoles }) {
    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }
    
    if (allowedRoles && !allowedRoles.includes(userRole)) {
        return <Navigate to="/unauthorized" replace />; // Trang báo lỗi 403
    }

    return <Outlet />;
}