import { Routes, Route, Navigate } from 'react-router-dom';
import DesktopLayout from '../components/layout/DesktopStaffLayout/DesktopLayout';
import PublicLayout from '../components/layout/PublicLayout/PublicLayout'; 
import PublicRoute from './PublicRoute';
import ProtectedRoute from './ProtectedRoute';
import { coachRoutes } from '../features/coaches';
import { publicTripRoutes } from '../features/trips';
import { routeRoutes } from '../features/routes';
import { cargoRoutes } from '../features/cargo';

const AppRouter = () => {
    const isAuthenticated = true; //để tạm bợ
    const userRole = 'MANAGER'; //để tạm bợ
    
    return (
        <Routes>
            <Route path="/" element={<PublicLayout />}>
                {publicTripRoutes}
            </Route>

            <Route element={<PublicRoute isAuthenticated={isAuthenticated} />}>
                <Route path="/login" element={<div>Trang Đăng Nhập</div>} />
            </Route>

            <Route element={
                <ProtectedRoute 
                    isAuthenticated={isAuthenticated} 
                    userRole={userRole} 
                    allowedRoles={['MANAGER', 'ADMIN']} 
                />
            }>
                <Route path="/manager" element={<DesktopLayout />}>
                    <Route index element={<Navigate to="dashboard" replace />} />
                    <Route path="dashboard" element={<div>Bảng điều khiển</div>} />
                    {coachRoutes}
                    {routeRoutes}
                    {cargoRoutes}
                </Route>
            </Route>

            {/* Catch-all route cho lỗi 404 */}
            <Route path="*" element={<div>404 - Không tìm thấy trang</div>} />
        </Routes>
    );
};

export default AppRouter;