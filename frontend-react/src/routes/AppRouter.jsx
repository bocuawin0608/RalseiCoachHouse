import { Routes, Route } from 'react-router-dom';
// Layouts
import PublicLayout from '../components/layout/PublicLayout/PublicLayout';
import AuthLayout from '../components/layout/AuthLayout/AuthLayout';
import StaffAuthLayout from '../components/layout/AuthLayout/StaffAuthLayout';
import DesktopLayout from '../components/layout/DesktopStaffLayout/DesktopLayout';
import MobileLayout from '../components/layout/MobileStaffLayout/MobileLayout';
// Guards
import GuestGuard from './GuestGuard';
import RoleGuard from './RoleGuard';
// Pages
import HomePage from '../pages/public/home/HomePage';
import Login from '../pages/auth/Login';
import Register from '../pages/auth/Register';
import StaffLogin from '../pages/auth/StaffLogin';
// Nested routes
import { publicTripRoutes } from '../features/trips';
import { routeRoutes } from '../features/routes';
import { cargoRoutes } from '../features/cargo';
import { coachRoutes } from '../features/coaches';

const AppRouter = () => {
    return (
        <Routes>
            <Route element={<PublicLayout />}>
                <Route path="/" element={<HomePage />} />
                <Route path="/unauthorized" element={<div>401 - Unauthorized.</div>} />
                {publicTripRoutes}
            </Route>

            <Route element={<GuestGuard />}>
                {/* Nhánh Auth của Customer */}
                <Route element={<AuthLayout />}>
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                </Route>

                {/* Nhánh Auth của Staff */}
                <Route element={<StaffAuthLayout />}>
                    <Route path="/staff/login" element={<StaffLogin />} />
                </Route>
            </Route>

            <Route element={<RoleGuard allowedRoles={['CUSTOMER']} />}>
                <Route element={<PublicLayout />}>
                    <Route path="/profile" element={<div>Trang cá nhân của khách</div>} />
                    <Route path="/booking-history" element={<div>Lịch sử đặt vé</div>} />
                </Route>
            </Route>

            <Route element={<RoleGuard allowedRoles={['MANAGER', 'ADMIN']} />}>
                <Route path="/management" element={<DesktopLayout />}>
                    <Route path="dashboard" element={<div>Bảng điều khiển quản trị</div>} />
                    {coachRoutes}
                    {routeRoutes}
                    {cargoRoutes}

                    {/* route dưới thì chỉ admin vào đc, manager thì ko */}
                    <Route element={<RoleGuard allowedRoles={['ADMIN']} />}>
                        <Route path="system-config" element={<div>Cấu hình hệ thống</div>} />
                        <Route path="manage-accounts" element={<div>Phân quyền tài khoản</div>} />
                    </Route>

                </Route>
            </Route>

            <Route path="/staff">
                <Route element={<RoleGuard allowedRoles={['TICKET_STAFF']} />}>
                    <Route element={<DesktopLayout />}>
                        <Route path="ticket/sell" element={<div>Bán vé</div>} />
                        <Route path="ticket/history" element={<div>Lịch sử bán</div>} />
                    </Route>
                </Route>

                <Route element={<RoleGuard allowedRoles={['TRIP_STAFF']} />}>
                    <Route element={<MobileLayout />}>
                        <Route path="trip/scan" element={<div>Quét vé</div>} />
                        <Route path="trip/status" element={<div>Cập nhật trạng thái xe</div>} />
                    </Route>
                </Route>
            </Route>

            <Route path="*" element={<div>404 - Page Not Found.</div>} />
        </Routes>
    );
};

export default AppRouter;