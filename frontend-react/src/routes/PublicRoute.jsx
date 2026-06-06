import { Navigate, Outlet } from 'react-router-dom';

export default function PublicRoute({ isAuthenticated }) {
    // Nếu đã đăng nhập, đá thẳng vào trang chủ của Manager
    if (isAuthenticated) {
        return <Navigate to="/manager/dashboard" replace />;
    }
    // Nếu chưa đăng nhập, cho phép render trang (Login, Register...)
    return <Outlet />;
}