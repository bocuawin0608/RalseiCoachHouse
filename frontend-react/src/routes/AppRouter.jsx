import { Routes, Route, Navigate } from 'react-router-dom'; 
import HomePage from '../pages/HomePage';
import CombinedLayout from '../components/layout/DesktopStaffLayout/CombinedLayout';
import PublicRoute from './PublicRoute';
import ProtectedRoute from './ProtectedRoute';
import { coachRoutes } from '../features/coaches';

const AppRouter = () => {
    // Mấy cái này sau lấy từ Context API or Redux
    const isAuthenticated = true; // Hardcode để test
    const userRole = 'MANAGER'; // Hardcode để test
    
    return (
        <Routes>
            {/* Hai cái này để tạm ntn */}
            <Route path="/" element={<Navigate to="/home" replace />} />
            <Route path="/home" element={<HomePage />} />


            {/* Luồng Public (Chưa đăng nhập) */}
            <Route element={<PublicRoute isAuthenticated={isAuthenticated} />}>
                <Route path="/login" element={<div>Trang Đăng Nhập</div>} />
            </Route>

            {/* Luồng Manager (Bắt buộc đăng nhập & Quyền MANAGER) */}
            <Route element={
                <ProtectedRoute 
                    isAuthenticated={isAuthenticated} 
                    userRole={userRole} 
                    allowedRoles={['MANAGER', 'ADMIN']} 
                />
            }>
                {/* Bọc ManagerLayout bên ngoài */}
                <Route path="/manager" element={<CombinedLayout />}>
                    {/* Đường dẫn mặc định khi vào /manager */}
                    <Route index element={<Navigate to="dashboard" replace />} />
                    <Route path="dashboard" element={<div>Bảng điều khiển</div>} />
                    {coachRoutes}
                </Route>
            </Route>

            {/* Catch-all route cho lỗi 404 */}
            <Route path="*" element={<div>404 - Không tìm thấy trang</div>} />
        </Routes>
    );
};

export default AppRouter;